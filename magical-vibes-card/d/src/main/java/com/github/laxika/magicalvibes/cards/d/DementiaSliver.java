package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "236")
public class DementiaSliver extends Card {

    public DementiaSliver() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        ActivatedAbility dementiaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCardNameAtResolutionEffect(),
                        RevealCardsFromHandEffect.atRandom(Subjects.targetPlayer(), 1),
                        new DiscardCardsEffect(Subjects.revealedThisWay(named))),
                "{T}: Choose a card name. Target opponent reveals a card at random from their hand. "
                        + "If that card has the chosen name, that player discards it. Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN);
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addStaticEffect(new GrantActivatedAbilityEffect(
                dementiaAbility,
                Subjects.otherCreatures(),
                sliver
        ));
        addStaticEffect(new GrantActivatedAbilityEffect(
                dementiaAbility,
                Subjects.source(),
                sliver
        ));
    }
}
