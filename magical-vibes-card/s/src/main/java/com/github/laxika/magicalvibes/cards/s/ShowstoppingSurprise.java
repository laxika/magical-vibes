package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceUpIfFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MKC", collectorNumber = "35")
@CardRegistration(set = "MKC", collectorNumber = "345")
public class ShowstoppingSurprise extends Card {

    public ShowstoppingSurprise() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addSpellEffect(new TurnTargetCreatureFaceUpIfFaceDownEffect())
                .addSpellEffect(new TargetCreatureDealsPowerDamageToEachOtherCreatureEffect());
    }
}
