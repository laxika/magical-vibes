package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "104")
public class DemonicTaskmaster extends Card {

    public DemonicTaskmaster() {
        // Flying is auto-loaded from Scryfall.

        // At the beginning of your upkeep, sacrifice a creature other than this creature.
        // Composite filter (not bare PermanentIsCreaturePredicate) so the source is excluded and
        // the multi-permanent sacrifice path is used; if no other creature is controlled, nothing
        // happens.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                SacrificeRecipient.CONTROLLER));
    }
}
