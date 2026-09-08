package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "108")
public class Cytoshape extends Card {

    private static final PermanentPredicate NONLEGENDARY_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
    ));

    public Cytoshape() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect(
                                NONLEGENDARY_CREATURE));
    }
}
