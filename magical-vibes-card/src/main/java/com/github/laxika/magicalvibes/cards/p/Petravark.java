package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "109")
public class Petravark extends Card {

    public Petravark() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentAndTrackWithSourceEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ReturnAllCardsExiledWithSourceEffect());
    }
}
