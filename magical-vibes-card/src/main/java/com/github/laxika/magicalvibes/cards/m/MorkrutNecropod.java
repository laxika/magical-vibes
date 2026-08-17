package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "125")
public class MorkrutNecropod extends Card {

    public MorkrutNecropod() {
        // Menace is auto-loaded from Scryfall.

        // Whenever this creature attacks or blocks, sacrifice another creature or land.
        addEffect(EffectSlot.ON_ATTACK, sacrificeAnotherCreatureOrLand());
        addEffect(EffectSlot.ON_BLOCK, sacrificeAnotherCreatureOrLand());
    }

    private SacrificePermanentsEffect sacrificeAnotherCreatureOrLand() {
        return new SacrificePermanentsEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                SacrificeRecipient.CONTROLLER);
    }
}
