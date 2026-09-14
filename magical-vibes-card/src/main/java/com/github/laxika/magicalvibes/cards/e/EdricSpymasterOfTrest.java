package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "VMA", collectorNumber = "251")
public class EdricSpymasterOfTrest extends Card {

    public EdricSpymasterOfTrest() {
        MayEffect draw = new MayEffect(
                new DrawCardForTriggeringPlayerEffect(1),
                "Draw a card?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT, draw);
    }
}
