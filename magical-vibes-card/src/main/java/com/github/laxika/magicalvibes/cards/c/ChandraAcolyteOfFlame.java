package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "126")
public class ChandraAcolyteOfFlame extends Card {

    public ChandraAcolyteOfFlame() {
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.LOYALTY,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED)),
                                new PermanentControlledBySourceControllerPredicate())),
                        EachPermanentScope.ALL_PLAYERS)),
                "0: Put a loyalty counter on each red planeswalker you control."));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new CreateTokenEffect(
                                2,
                                "Elemental",
                                1,
                                1,
                                CardColor.RED,
                                Set.of(CardColor.RED),
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(),
                                Set.of(Keyword.HASTE)),
                        new SacrificeCreatedPermanentsAtEndStepEffect()),
                "0: Create two 1/1 red Elemental creature tokens. They gain haste. Sacrifice them at the beginning of the next end step."));

        TargetFilter targetFilter = new GraveyardCardPredicateTargetFilter(
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new CardMaxManaValuePredicate(3))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false,
                        true)),
                "−2: You may cast target instant or sorcery card with mana value 3 or less from your graveyard. If that spell would be put into your graveyard, exile it instead.",
                targetFilter));
    }
}
