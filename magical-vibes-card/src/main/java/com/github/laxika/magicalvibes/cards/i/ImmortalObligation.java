package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "10")
@CardRegistration(set = "MKC", collectorNumber = "321")
public class ImmortalObligation extends Card {

    public ImmortalObligation() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                .targetGraveyard(true)
                .underOwnersControl(true)
                .enterWithCounter(CounterType.DUTY)
                .enterWithCounterCount(1)
                .build());
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.OPPONENT_GRAVEYARD));

        addEffect(EffectSlot.SPELL, new GrantEffectsToCounterBearersEffect(CounterType.DUTY, List.of(
                new GoadCreaturesUntilNextTurnEffect(new PermanentHasCountersPredicate(CounterType.DUTY)),
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()), true),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                        new PermanentHasCountersPredicate(CounterType.DUTY),
                        new PermanentControlledBySourceControllerPredicate(),
                        "Creatures with duty counters can't block creatures you control"))));
    }
}
