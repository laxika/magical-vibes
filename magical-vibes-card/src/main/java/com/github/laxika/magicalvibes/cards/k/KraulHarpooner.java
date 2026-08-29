package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "136")
public class KraulHarpooner extends Card {

    public KraulHarpooner() {
        CardsInGraveyard creatureCardsInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        PermanentPredicate flyingCreatureAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                flyingCreatureAnOpponentControls,
                "Target must be a creature with flying an opponent controls"
        ), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new BoostSelfEffect(creatureCardsInGraveyard, new Fixed(0)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(
                                new TargetPermanentMatches(flyingCreatureAnOpponentControls),
                                new MayEffect(
                                        new EnteringCreatureFightsTargetCreatureEffect(),
                                        "Have Kraul Harpooner fight that creature?"
                                )
                        ));
    }
}
