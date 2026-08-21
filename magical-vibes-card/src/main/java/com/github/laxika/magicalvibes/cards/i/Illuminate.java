package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "63")
public class Illuminate extends Card {

    public Illuminate() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{R}"));
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.singlePayment(List.of("{3}{U}")));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                        new DealDamageToPlayersEffect(new XValue(), DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new RepeatedAdditionalCostPaid("{3}{U}"),
                new DrawCardEffect(new XValue())));
    }
}
