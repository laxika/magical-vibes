package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "KTK", collectorNumber = "30")
public class WatcherOfTheRoost extends Card {

    public WatcherOfTheRoost() {
        addMorph("{2}{W}", new CardColorPredicate(CardColor.WHITE), "white");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new GainLifeEffect(2));
    }
}
