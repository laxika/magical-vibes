package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Perpetually grants a graveyard-activated ability to one creature card in a graveyard: a targeted one, or the card the
 * trigger event was about.
 *
 * @param ability        the ability granted
 * @param triggeringCard whether the card is the trigger event's card ("it perpetually gains unearth", Consumed by
 *                       History), which isn't targeted, in the graveyard incarnation it had as the ability triggered
 *                       (CR 400.7), rather than a target creature card in the controller's graveyard
 */
public record PerpetuallyGrantGraveyardAbilityToTargetCardEffect(ActivatedAbility ability, boolean triggeringCard)
        implements CardEffect {

    /** The grant of {@code ability} to target creature card in its controller's graveyard. */
    public PerpetuallyGrantGraveyardAbilityToTargetCardEffect(ActivatedAbility ability) {
        this(ability, false);
    }

    /** The grant of {@code ability} to the trigger event's card. */
    public static PerpetuallyGrantGraveyardAbilityToTargetCardEffect toTriggeringCard(ActivatedAbility ability) {
        return new PerpetuallyGrantGraveyardAbilityToTargetCardEffect(ability, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return triggeringCard ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
