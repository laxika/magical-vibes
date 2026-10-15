package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "121")
public class WasteLandPlaytest extends Card {

    public WasteLandPlaytest() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Sacrifice Waste Land: Destroy target nonbasic land. That land's controller creates
        // a Wastes token.
        CreateTokenEffect wastesToken = new CreateTokenEffect(
                CardType.LAND,
                1,
                "Wastes",
                0,
                0,
                null,
                null,
                List.of(),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(ManaAbilities.tapFor(ManaColor.COLORLESS)),
                false,
                false,
                false,
                0,
                Set.of());
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentThenEffect(wastesToken, ThenEffectRecipient.TARGET_CONTROLLER)
                ),
                "{T}, Sacrifice Waste Land: Destroy target nonbasic land. That land's controller creates a Wastes token.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                        )),
                        "Target must be a nonbasic land"
                )
        ));
    }
}
