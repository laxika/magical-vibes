package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "231")
@CardRegistration(set = "SOS", collectorNumber = "356")
public class SplatterTechnique extends Card {

    public SplatterTechnique() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Draw four cards",
                        new DrawCardEffect(4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Splatter Technique deals 4 damage to each creature and planeswalker",
                        new MassDamageEffect(4, false, false, true, null))
        )));
    }
}
