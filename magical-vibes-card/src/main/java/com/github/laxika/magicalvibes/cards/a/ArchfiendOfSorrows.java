package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MH2", collectorNumber = "74")
public class ArchfiendOfSorrows extends Card {

    public ArchfiendOfSorrows() {
        // When this creature enters, creatures your opponents control get -2/-2 until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        // Unearth {3}{B}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{3}{B}{B}");
    }
}
