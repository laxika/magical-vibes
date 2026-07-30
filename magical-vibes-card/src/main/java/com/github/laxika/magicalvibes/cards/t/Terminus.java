package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.PutAllPermanentsOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AVR", collectorNumber = "38")
public class Terminus extends Card {

    public Terminus() {
        // Miracle {W}
        addCastingOption(new MiracleCast("{W}"));

        // Put all creatures on the bottom of their owners' libraries.
        addEffect(EffectSlot.SPELL, new PutAllPermanentsOnBottomOfLibraryEffect(new PermanentIsCreaturePredicate()));
    }
}
