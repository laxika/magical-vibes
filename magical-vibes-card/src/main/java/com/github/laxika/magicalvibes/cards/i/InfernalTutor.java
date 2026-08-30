package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffect;

@CardRegistration(set = "DIS", collectorNumber = "46")
public class InfernalTutor extends Card {

    public InfernalTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffect());
    }
}
