package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "37")
public class RuleOfLaw extends Card {

    public RuleOfLaw() {
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1));
    }
}
