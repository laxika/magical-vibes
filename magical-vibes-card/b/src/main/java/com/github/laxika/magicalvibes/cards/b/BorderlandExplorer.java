package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardThenSearchBasicLandToHandEffect;

@CardRegistration(set = "C18", collectorNumber = "133")
public class BorderlandExplorer extends Card {

    public BorderlandExplorer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerMayDiscardThenSearchBasicLandToHandEffect());
    }
}
