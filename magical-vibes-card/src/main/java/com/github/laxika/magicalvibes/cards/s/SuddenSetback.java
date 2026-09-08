package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

@CardRegistration(set = "MKM", collectorNumber = "72")
public class SuddenSetback extends Card {

    public SuddenSetback() {
        addEffect(EffectSlot.SPELL, new PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new StackEntryTruePredicate()));
    }
}
