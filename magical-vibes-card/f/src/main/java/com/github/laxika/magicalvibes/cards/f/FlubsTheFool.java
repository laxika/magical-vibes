package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "356")
public class FlubsTheFool extends Card {

    public FlubsTheFool() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, drawOrDiscard());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(drawOrDiscard())));
    }

    private static ConditionalReplacementEffect drawOrDiscard() {
        return new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new DrawCardEffect(1));
    }
}
