package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "2")
public class ArmedResponse extends Card {

    public ArmedResponse() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                                CountScope.CONTROLLER)));
    }
}
