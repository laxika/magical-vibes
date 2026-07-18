package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardToTopOfLibraryInsteadEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "4ED", collectorNumber = "333")
@CardRegistration(set = "5ED", collectorNumber = "387")
public class LibraryOfLeng extends Card {

    public LibraryOfLeng() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.STATIC, new DiscardToTopOfLibraryInsteadEffect());
    }
}
