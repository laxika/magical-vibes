package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutAllPermanentsOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "7")
public class HallowedBurial extends Card {

    public HallowedBurial() {
        addEffect(EffectSlot.SPELL, new PutAllPermanentsOnBottomOfLibraryEffect(new PermanentIsCreaturePredicate()));
    }
}
