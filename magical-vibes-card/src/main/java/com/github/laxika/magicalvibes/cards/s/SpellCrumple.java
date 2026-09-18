package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;

@CardRegistration(set = "CMD", collectorNumber = "63")
public class SpellCrumple extends Card {

    public SpellCrumple() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.LIBRARY_BOTTOM));
        addEffect(EffectSlot.SPELL, new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
