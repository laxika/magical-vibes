package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DTK", collectorNumber = "153")
public class SarkhansRage extends Card {

    public SarkhansRage() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)));
    }
}
