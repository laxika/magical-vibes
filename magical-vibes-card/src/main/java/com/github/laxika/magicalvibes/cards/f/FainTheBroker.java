package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "40")
public class FainTheBroker extends Card {

    public FainTheBroker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "{T}, Sacrifice a creature: Put two +1/+1 counters on target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(1, new PermanentIsCreaturePredicate(), false),
                        CreateTokenEffect.ofTreasureToken(1)
                ),
                "{T}, Remove a counter from a creature you control: Create a Treasure token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new CreateTokenEffect(
                                1,
                                "Inkling",
                                2,
                                1,
                                CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.INKLING),
                                Set.of(Keyword.FLYING),
                                Set.of())
                ),
                "{T}, Sacrifice an artifact: Create a 2/1 white and black Inkling creature token with flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}{B}: Untap Fain."
        ));
    }
}
