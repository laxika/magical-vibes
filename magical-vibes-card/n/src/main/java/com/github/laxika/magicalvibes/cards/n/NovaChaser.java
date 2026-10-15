package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;

@CardRegistration(set = "LRW", collectorNumber = "187")
public class NovaChaser extends Card {

    public NovaChaser() {
        // Trample is auto-loaded from Scryfall.
        // Champion an Elemental.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.ELEMENTAL));
    }
}
