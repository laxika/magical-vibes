package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameCreatureForEachControlledCreatureEffect;

@CardRegistration(set = "M12", collectorNumber = "170")
public class DoublingChant extends Card {

    public DoublingChant() {
        // For each creature you control, you may search your library for a creature card with the
        // same name as that creature. Put those cards onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL,
                new SearchLibraryForSameNameCreatureForEachControlledCreatureEffect());
    }
}
