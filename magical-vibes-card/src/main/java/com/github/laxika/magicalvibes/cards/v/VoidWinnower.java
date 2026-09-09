package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "17")
public class VoidWinnower extends Card {

    public VoidWinnower() {
        addEffect(EffectSlot.STATIC,
                new OpponentsCantCastSpellsMatchingPredicateEffect(
                        new CardManaValueParityPredicate(ManaValueParity.EVEN)));

        addEffect(EffectSlot.STATIC,
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentManaValueParityPredicate(ManaValueParity.EVEN),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        new PermanentIsCreaturePredicate(),
                        "Opponents can't block with creatures with even mana values"));
    }
}
