package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

/**
 * Registers a delayed trigger: when the targeted creature or artifact is put into a graveyard this turn, return that card to the
 * battlefield (tapped if {@code enterTapped}) under its owner's control, or under the ability
 * controller's control when {@code returnUnderController} is true. When
 * {@code requireControllerGraveyard} is true, the delayed trigger only fires if the card enters
 * the registering ability controller's graveyard.
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records the
 * permanent's card ID in {@code GameData.creaturesReturnedToBattlefieldOnDeathThisTurn}. When that
 * permanent is put into a graveyard later in the same turn, the death pipeline pushes a triggered ability that returns
 * the card from its owner's graveyard to the battlefield. Used by Graceful Reprieve, Supernatural
 * Stamina, Adarkar Valkyrie, Melira, the Living Cure, and Saffi Eriksdotter.
 */
public record ReturnTargetCardOnDeathThisTurnEffect(boolean enterTapped, boolean returnUnderController,
                                                    boolean excludeSource, boolean requireControllerGraveyard) implements CardEffect {

    public ReturnTargetCardOnDeathThisTurnEffect() {
        this(false, false, false, false);
    }

    /** Convenience for the owner's-control return (Graceful Reprieve, Supernatural Stamina). */
    public ReturnTargetCardOnDeathThisTurnEffect(boolean enterTapped) {
        this(enterTapped, false, false, false);
    }

    /** Convenience for Adarkar Valkyrie's untapped return under the ability controller's control. */
    public static ReturnTargetCardOnDeathThisTurnEffect underControllerExcludingSource() {
        return new ReturnTargetCardOnDeathThisTurnEffect(false, true, true, false);
    }

    /** Convenience for Saffi Eriksdotter's "your graveyard" delayed return. */
    public static ReturnTargetCardOnDeathThisTurnEffect fromControllerGraveyard() {
        return new ReturnTargetCardOnDeathThisTurnEffect(false, false, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                excludeSource ? TargetPredicates.creature() : TargetPredicates.permanent(),
                excludeSource ? new PermanentNotPredicate(new PermanentIsSourceCardPredicate()) : null
        );
    }
}
