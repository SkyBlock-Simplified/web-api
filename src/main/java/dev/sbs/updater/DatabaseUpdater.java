package dev.sbs.updater;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.request.HypixelRequest;
import dev.sbs.api.data.sql.SqlConfig;
import dev.sbs.api.util.helper.ResourceUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.updater.processor.resource.ResourceCollectionsProcessor;
import dev.sbs.updater.processor.resource.ResourceItemsProcessor;
import dev.sbs.updater.processor.resource.ResourceSkillsProcessor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

@Getter
@Log4j2
public class DatabaseUpdater {

    private static final HypixelRequest HYPIXEL_RESOURCE_REQUEST = SimplifiedApi.getApiRequest(HypixelRequest.class);

    public DatabaseUpdater() {
        ResourceUtil.getEnv("HYPIXEL_API_KEY")
            .map(StringUtil::toUUID)
            .ifPresent(value -> SimplifiedApi.getKeyManager().add("HYPIXEL_API_KEY", value));

        Configurator.setLevel(log, Level.INFO);
        log.info("Connecting to Database");
        SimplifiedApi.getSessionManager().connect(SqlConfig.defaultSql());
        log.info("Database Initialized in {}ms", SimplifiedApi.getSessionManager().getSession().getInitializationTime());
        log.info("Database Cached in {}ms", SimplifiedApi.getSessionManager().getSession().getStartupTime());

        log.info("Loading Processors");
        ResourceItemsProcessor itemsProcessor = new ResourceItemsProcessor(HYPIXEL_RESOURCE_REQUEST.getItems());
        ResourceSkillsProcessor skillsProcessor = new ResourceSkillsProcessor(HYPIXEL_RESOURCE_REQUEST.getSkills());
        ResourceCollectionsProcessor collectionsProcessor = new ResourceCollectionsProcessor(HYPIXEL_RESOURCE_REQUEST.getCollections());

        try {
            log.info("Processing Items");
            itemsProcessor.process();
            log.info("Processing Skills");
            skillsProcessor.process();
            log.info("Processing Collections");
            collectionsProcessor.process();
        } catch (Exception exception) {
            log.atError()
                .withThrowable(exception)
                .log("An error occurred while processing the resource API.");
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        new DatabaseUpdater();
    }

}
