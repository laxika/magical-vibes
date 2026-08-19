package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "2")
public class BishopOfBinding extends Card {

    public BishopOfBinding() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                "Target must be a Vampire"
        )).addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(
                new TotalPowerOfCardsExiledWithSource(), new TotalPowerOfCardsExiledWithSource()));
    }
}
