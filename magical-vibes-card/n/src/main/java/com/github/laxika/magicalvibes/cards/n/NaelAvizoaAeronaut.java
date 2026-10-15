package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.BasicLandTypesAmongControlledLandsAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DMU", collectorNumber = "207")
public class NaelAvizoaAeronaut extends Card {

    public NaelAvizoaAeronaut() {
        // Domain — Whenever Nael deals combat damage to a player, look at the top X cards of your
        // library, where X is the number of basic land types among lands you control. Put up to one
        // of them on top of your library and the rest on the bottom in a random order. Then if there
        // are five basic land types among lands you control, draw a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new LookAtTopCardsEffect(
                        new BasicLandTypesAmongControlledLands(), new Fixed(1), null,
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                        LibrarySearchDestination.TOP_OF_LIBRARY, true),
                ConditionalEffect.unless(
                        new BasicLandTypesAmongControlledLandsAtLeast(5), new DrawCardEffect(1))));
    }
}
