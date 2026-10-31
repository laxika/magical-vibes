package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;

@CardRegistration(set = "CMD", collectorNumber = "63")
@CardRegistration(set = "CM2", collectorNumber = "50")
public class SpellCrumple extends Card {

    public SpellCrumple() {
        addSpellEffect(new CounterSpellEffect(CounteredSpellDestination.LIBRARY_BOTTOM));
        addSpellEffect(new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
