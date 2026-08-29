package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "BNG", collectorNumber = "155")
public class SirenOfTheSilentSong extends Card {

    public SirenOfTheSilentSong() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
