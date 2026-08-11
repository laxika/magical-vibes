package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;

@CardRegistration(set = "DST", collectorNumber = "36")
public class Vex extends Card {

    public Vex() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new TargetSpellControllerDrawsCardEffect(),
                "Draw a card?",
                null,
                MayChoicePlayer.TARGET_SPELL_CONTROLLER));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
