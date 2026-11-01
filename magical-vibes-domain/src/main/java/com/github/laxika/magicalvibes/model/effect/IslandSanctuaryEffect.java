package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/**
 * Island Sanctuary's static ability: "If you would draw a card during your draw step, instead you may skip
 * that draw. If you do, until your next turn, you can't be attacked except by creatures with flying and/or
 * islandwalk." {@code StepTriggerService.handleDrawStep} offers the skip for the turn-based draw and
 * {@code DrawService} for any other draw in that step; skipping stamps a player-scoped
 * {@code CreaturesCantAttackControllerUnlessPredicateEffect} floating effect until the controller's next turn.
 */
public record IslandSanctuaryEffect() implements MaySkipDrawReplacementEffect {

    @Override
    public DrawReplacementKind replacementKind() {
        return DrawReplacementKind.ISLAND_SANCTUARY;
    }

    @Override
    public boolean drawStepOnly() {
        return true;
    }
}
