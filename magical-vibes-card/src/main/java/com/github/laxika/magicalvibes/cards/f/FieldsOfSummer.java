package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "10")
public class FieldsOfSummer extends Card {

    public FieldsOfSummer() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(null, List.of(
                new MayEffect(new GainLifeEffect(new Fixed(2), GainLifeRecipient.TRIGGERING_PLAYER),
                        "Gain 2 life?", null, MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER))));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(new GainLifeEffect(10), "Gain 10 life?"));
    }
}
