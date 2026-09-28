package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfAllLandsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;

@CardRegistration(set = "C20", collectorNumber = "68")
public class ManascapeRefractor extends Card {

    public ManascapeRefractor() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfAllLandsEffect());
        addEffect(EffectSlot.STATIC, new SpendManaAsAnyColorForActivatedAbilitiesEffect());
    }
}
