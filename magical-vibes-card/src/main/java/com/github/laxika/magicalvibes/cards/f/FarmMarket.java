package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.Market;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Farm // Market — front half (Farm).
 * Instant — Destroy target attacking or blocking creature.
 * Back half (Market) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "148")
public class FarmMarket extends Card {

    public FarmMarket() {
        Market market = new Market();
        market.setSetCode(getSetCode());
        market.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(market);

        // Destroy target attacking or blocking creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be an attacking or blocking creature"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Market";
    }
}
