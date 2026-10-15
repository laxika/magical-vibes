package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "HBG", collectorNumber = "205")
public class DraconicMuralists extends Card {

    public DraconicMuralists() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DRAGON)),
                "Search your library for a Dragon card?"));
    }
}
