package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "191")
public class GeometersArthropod extends Card {

    public GeometersArthropod() {
        // Whenever you cast a spell with {X} in its mana cost, look at the top X cards of your
        // library. Put one of them into your hand and the rest on the bottom of your library in a
        // random order.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new LookAtTopCardsEffect(
                        new XValue(),
                        new Fixed(1),
                        null,
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false
                )),
                new StackEntryHasXInManaCostPredicate()
        ));
    }
}
