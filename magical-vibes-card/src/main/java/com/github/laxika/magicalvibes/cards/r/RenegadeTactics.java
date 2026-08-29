package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "KLD", collectorNumber = "127")
public class RenegadeTactics extends Card {

    public RenegadeTactics() {
        addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
