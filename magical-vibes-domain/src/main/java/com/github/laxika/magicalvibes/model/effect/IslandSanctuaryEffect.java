package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in the {@code MAY_SKIP_DRAW_STEP_DRAW} slot. Its presence lets the
 * controller skip their turn-based draw-step draw; when they do, they gain a temporary
 * "can't be attacked except by creatures with flying and/or islandwalk" shield until their
 * next turn (stamped as a player-scoped {@code CreaturesCantAttackControllerUnlessPredicateEffect}
 * floating effect). Never resolved directly — detected by slot presence in
 * {@code StepTriggerService.handleDrawStep}. Used by Island Sanctuary.
 */
public record IslandSanctuaryEffect() implements CardEffect {
}
