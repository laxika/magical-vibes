package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "HBG", collectorNumber = "238")
public class KorlessaScaleSinger extends Card {

    public KorlessaScaleSinger() {
        // "You may look at the top card of your library any time."
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        // "You may cast Dragon spells from the top of your library."
        addEffect(EffectSlot.STATIC,
                new AllowCastFromTopOfLibraryEffect(new CardSubtypePredicate(CardSubtype.DRAGON)));
    }
}
