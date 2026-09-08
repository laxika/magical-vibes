package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Return;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Never // Return — front half (Never).
 * Sorcery — Destroy target creature or planeswalker.
 * Back half (Return) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "212")
@CardRegistration(set = "AKR", collectorNumber = "118")
public class NeverReturn extends Card {

    public NeverReturn() {
        setBackFaceCard(new Return());

        // Destroy target creature or planeswalker.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature or planeswalker"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Return";
    }
}
