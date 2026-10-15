package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "3")
@CardRegistration(set = "BRC", collectorNumber = "42")
public class TawnosSolemnSurvivor extends Card {

    public TawnosSolemnSurvivor() {
        // {2}, {T}: Create a token that's a copy of up to one target artifact token you control.
        // Mill two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new MillEffect(2, MillRecipient.CONTROLLER)
                ),
                "{2}, {T}: Create a token that's a copy of up to one target artifact token you control. Mill two cards.",
                List.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsTokenPredicate()
                        )),
                        "Target must be an artifact token you control.")),
                0,
                1
        ));

        // {1}{W}{U}{B}, {T}, Sacrifice two artifact tokens, Exile an artifact or creature card from
        // your graveyard: Create a token that's a copy of the exiled card, except it's an artifact
        // in addition to its other types. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{U}{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsTokenPredicate()
                        ))),
                        new ExileCardFromGraveyardCost(
                                CardType.ARTIFACT,
                                false,
                                true,
                                false,
                                null,
                                CardType.CREATURE),
                        new CreateTokenCopyOfImprintedCardEffect(
                                false,
                                false,
                                List.of(),
                                Set.of(CardType.ARTIFACT),
                                null,
                                null,
                                false,
                                List.of(),
                                false)
                ),
                "{1}{W}{U}{B}, {T}, Sacrifice two artifact tokens, Exile an artifact or creature card from your graveyard: "
                        + "Create a token that's a copy of the exiled card, except it's an artifact in addition to its other types. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
