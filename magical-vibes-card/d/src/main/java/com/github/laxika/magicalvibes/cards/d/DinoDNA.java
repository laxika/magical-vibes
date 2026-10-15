package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "REX", collectorNumber = "20")
@CardRegistration(set = "REX", collectorNumber = "45")
public class DinoDNA extends Card {

    public DinoDNA() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        GraveyardSearchScope allGraveyards = GraveyardSearchScope.ALL_GRAVEYARDS;

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(
                        creatureCard, allGraveyards)),
                "{1}, {T}: Exile target creature card from a graveyard. Activate only as a sorcery.",
                new GraveyardCardPredicateTargetFilter(creatureCard, allGraveyards),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));

        CreateTokenCopyOfTargetPermanentEffect tokenCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(),
                Set.of(),
                6,
                6,
                Map.of(),
                false,
                false,
                false,
                false,
                false,
                false,
                CardColor.GREEN,
                Set.of(Keyword.TRAMPLE),
                false,
                Map.of(),
                List.of(CardSubtype.DINOSAUR),
                false,
                false,
                new Fixed(1),
                false,
                Set.of(),
                false,
                List.of());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopy, true)),
                "{6}: Create a token that's a copy of target creature card exiled with this artifact, except it's a 6/6 green Dinosaur creature with trample. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
