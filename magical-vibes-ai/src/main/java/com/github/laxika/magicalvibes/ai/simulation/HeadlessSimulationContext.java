package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.carddata.CardDataConfiguration;
import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Long-lived Spring context for headless MCTS simulation. Built once per JVM and reused;
 * all services are stateless with respect to {@code GameData} parameters.
 *
 * <p>Card data comes from the live application when it has been {@linkplain #shareCardData shared}
 * before the context is built, so starting the first hard-AI game does not load every set a second
 * time. Without it — standalone AI tests — the context loads its own card data.
 */
public final class HeadlessSimulationContext {

    private static volatile AnnotationConfigApplicationContext context;
    private static volatile GameSimulator simulator;
    private static volatile SharedCardData sharedCardData;

    private HeadlessSimulationContext() {
    }

    /**
     * Makes the context use these already-loaded card-data beans instead of building its own. Has
     * no effect once the context has been built.
     */
    public static void shareCardData(CardRegistry cardRegistry, RandomDeckGenerator randomDeckGenerator,
                                     DeckLegalityRegistry deckLegalityRegistry) {
        sharedCardData = new SharedCardData(cardRegistry, randomDeckGenerator, deckLegalityRegistry);
    }

    /**
     * Returns the cached headless simulator, shared by every live game. {@link GameQueryService}
     * instances from the same {@link com.github.laxika.magicalvibes.service.GameEngineConfig} graph
     * are behaviorally identical, so the live game's bean and this context's bean are
     * interchangeable for MCTS — a per-game simulator would buy nothing.
     */
    public static GameSimulator getSimulator() {
        GameSimulator existing = simulator;
        if (existing != null) {
            return existing;
        }
        synchronized (HeadlessSimulationContext.class) {
            existing = simulator;
            if (existing != null) {
                return existing;
            }
            AnnotationConfigApplicationContext ctx = getContext();
            existing = new GameSimulator(
                    ctx.getBean(GameService.class),
                    ctx.getBean(GameQueryService.class),
                    ctx.getBean(BlockLegalityService.class),
                    ctx.getBean(GameActionAvailabilityService.class),
                    ctx.getBean(com.github.laxika.magicalvibes.service.cast.CastingCostService.class),
                    ctx.getBean(GameRegistry.class),
                    ctx.getBean(CombatAttackService.class),
                    ctx.getBean(ValidTargetService.class));
            simulator = existing;
            return existing;
        }
    }

    static AnnotationConfigApplicationContext getContext() {
        AnnotationConfigApplicationContext existing = context;
        if (existing != null) {
            return existing;
        }
        synchronized (HeadlessSimulationContext.class) {
            existing = context;
            if (existing != null) {
                return existing;
            }
            AnnotationConfigApplicationContext created = build(sharedCardData);
            context = created;
            return created;
        }
    }

    /** Builds a headless context on {@code shared}, or on card data of its own when it is null. */
    static AnnotationConfigApplicationContext build(SharedCardData shared) {
        AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext();
        created.register(HeadlessSimulationDoublesConfig.class);
        if (shared != null) {
            // Registered as finished singletons: Spring neither re-runs their initialization
            // (CardRegistry's set loading) nor destroys them with this context.
            ConfigurableListableBeanFactory beanFactory = created.getBeanFactory();
            beanFactory.registerSingleton("cardRegistry", shared.cardRegistry());
            beanFactory.registerSingleton("randomDeckGenerator", shared.randomDeckGenerator());
            beanFactory.registerSingleton("deckLegalityRegistry", shared.deckLegalityRegistry());
        } else {
            created.register(CardDataConfiguration.class);
        }
        created.refresh();
        return created;
    }

    record SharedCardData(CardRegistry cardRegistry, RandomDeckGenerator randomDeckGenerator,
                          DeckLegalityRegistry deckLegalityRegistry) {
    }
}
