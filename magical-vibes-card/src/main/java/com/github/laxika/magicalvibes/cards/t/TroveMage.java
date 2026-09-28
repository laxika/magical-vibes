package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YDMU", collectorNumber = "6")
public class TroveMage extends Card {

    public TroveMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SeekFromTopOfLibraryEffect(10, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
