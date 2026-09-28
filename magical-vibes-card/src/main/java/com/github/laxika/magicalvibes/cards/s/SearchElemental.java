package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MB2", collectorNumber = "299")
@CardRegistration(set = "MB2", collectorNumber = "535")
public class SearchElemental extends Card {

    public SearchElemental() {
        addEffect(EffectSlot.ON_CONTROLLER_SEARCHES_LIBRARY, new ScryEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new MakeCreatureUnblockableEffect(true)));
    }
}
