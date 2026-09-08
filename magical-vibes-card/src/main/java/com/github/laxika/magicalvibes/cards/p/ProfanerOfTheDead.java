package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect;

@CardRegistration(set = "DTK", collectorNumber = "70")
public class ProfanerOfTheDead extends Card {

    public ProfanerOfTheDead() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));
        addEffect(EffectSlot.ON_EXPLOIT,
                new ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect());
    }
}
