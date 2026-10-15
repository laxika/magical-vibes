package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BrilliantUltimatumEffect;

@CardRegistration(set = "ALA", collectorNumber = "159")
public class BrilliantUltimatum extends Card {

    public BrilliantUltimatum() {
        addEffect(EffectSlot.SPELL, new BrilliantUltimatumEffect(5));
    }
}
