package com.github.laxika.magicalvibes.model.effect;

/**
 * Dubious Challenge: look at the top ten cards of the controller's library, exile up to two
 * creature cards from among them, then let the targeted opponent choose one of those cards to put
 * onto the battlefield under their control. The remaining exiled cards enter under the controller's
 * control.
 */
public record DubiousChallengeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
