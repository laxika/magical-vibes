package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "EOE", collectorNumber = "137")
public class InvasiveManeuvers extends Card {

    public InvasiveManeuvers() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                new FixedIfCondition(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SPACECRAFT)),
                        5,
                        3)));
    }
}
