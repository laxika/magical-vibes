package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.Dust;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Grind // Dust — front half (Grind).
 * Sorcery — Put a -1/-1 counter on target creature. That creature can't block this turn.
 * Back half (Dust) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "155")
public class GrindDust extends Card {

    public GrindDust() {
        Dust dust = new Dust();
        dust.setSetCode(getSetCode());
        dust.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(dust);

        // Put a -1/-1 counter on target creature. That creature can't block this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "Dust";
    }
}
