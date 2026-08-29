package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

@CardRegistration(set = "USG", collectorNumber = "188")
public class Gamble extends Card {

    public Gamble() {
        addEffect(EffectSlot.SPELL, SearchLibraryEffect.withDeferredShuffle());
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER, true));
        addEffect(EffectSlot.SPELL, new ShuffleLibraryEffect(false));
    }
}
