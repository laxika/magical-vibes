package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "103")
public class ImperialHellkite extends Card {

    public ImperialHellkite() {
        addMorph("{6}{R}{R}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DRAGON)),
                "Search your library for a Dragon card?"
        ));
    }
}
