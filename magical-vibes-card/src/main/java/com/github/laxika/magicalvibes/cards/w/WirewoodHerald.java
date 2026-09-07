package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "302")
public class WirewoodHerald extends Card {

    public WirewoodHerald() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ELF)),
                "Search your library for an Elf card?"
        ));
    }
}
