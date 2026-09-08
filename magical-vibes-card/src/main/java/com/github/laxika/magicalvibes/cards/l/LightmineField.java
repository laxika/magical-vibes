package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/**
 * Lightmine Field — whenever one or more creatures attack, it deals damage to each attacking
 * creature equal to the number of attacking creatures.
 */
@CardRegistration(set = "ROE", collectorNumber = "32")
public class LightmineField extends Card {

    public LightmineField() {
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new DealDamageToEachMatchingPermanentEffect(
                new PermanentCount(attacking, CountScope.ANY_PLAYER),
                attacking,
                EachPermanentScope.ALL_PLAYERS));
    }
}
