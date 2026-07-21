package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.Oblivion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Consign // Oblivion — front half (Consign).
 * Instant — Return target nonland permanent to its owner's hand.
 * Back half (Oblivion) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "149")
public class ConsignOblivion extends Card {

    public ConsignOblivion() {
        Oblivion oblivion = new Oblivion();
        oblivion.setSetCode(getSetCode());
        oblivion.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(oblivion);

        // Return target nonland permanent to its owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }

    @Override
    public String getBackFaceClassName() {
        return "Oblivion";
    }
}
