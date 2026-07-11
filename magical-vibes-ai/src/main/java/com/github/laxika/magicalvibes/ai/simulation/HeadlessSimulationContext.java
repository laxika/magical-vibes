package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Long-lived Spring context for headless MCTS simulation. Built once per JVM and reused;
 * all services are stateless with respect to {@code GameData} parameters.
 */
public final class HeadlessSimulationContext {

    private static volatile AnnotationConfigApplicationContext context;
    private static volatile GameSimulator simulator;

    private HeadlessSimulationContext() {
    }

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
                    ctx.getBean(GameBroadcastService.class),
                    ctx.getBean(com.github.laxika.magicalvibes.service.cast.CastingCostService.class),
                    ctx.getBean(GameRegistry.class),
                    ctx.getBean(CombatAttackService.class));
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
            AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext(HeadlessSimulationDoublesConfig.class);
            context = created;
            return created;
        }
    }
}
