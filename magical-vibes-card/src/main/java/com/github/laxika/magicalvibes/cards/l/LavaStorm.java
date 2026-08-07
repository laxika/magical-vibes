package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "110")
public class LavaStorm extends Card {

    public LavaStorm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Lava Storm deals 2 damage to each attacking creature",
                        new DealDamageToEachMatchingPermanentEffect(2, new PermanentIsAttackingPredicate(), EachPermanentScope.ALL_PLAYERS)),
                new ChooseOneEffect.ChooseOneOption("Lava Storm deals 2 damage to each blocking creature",
                        new DealDamageToEachMatchingPermanentEffect(2, new PermanentIsBlockingPredicate(), EachPermanentScope.ALL_PLAYERS))
        )));
    }
}
