package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "208")
public class PriestOfFellRites extends Card {

    public PriestOfFellRites() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(3),
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()
                ),
                "{T}, Pay 3 life, Sacrifice this creature: Return target creature card from your graveyard to the battlefield. "
                        + "Activate only as a sorcery.",
                new GraveyardCardPredicateTargetFilter(
                        new CardTypePredicate(CardType.CREATURE),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addUnearth("{3}{W}{B}");
    }
}
