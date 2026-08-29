package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Moves the targeted creature card from the controller's graveyard to the top of their library,
 * then deals damage equal to that card's power to the targeted creature.
 *
 * <p>The graveyard card's power is captured before it changes zones. The two target groups are
 * resolved independently: a graveyard card that has left the graveyard prevents both parts, while
 * a battlefield creature that is no longer legal only prevents the damage.</p>
 */
public record PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect(
        int graveyardTargetGroup, int creatureTargetGroup) implements CardEffect {

    public PutTargetCreatureCardFromGraveyardOnTopAndDealPowerDamageEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
