package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledLandsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "231")
public class NissaOfShadowedBoughs extends Card {

    public NissaOfShadowedBoughs() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.LOYALTY));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new UntapPermanentsEffect(Subjects.target()),
                        new MayEffect(
                                new AnimatePermanentsEffect(
                                        3, 3, List.of(CardSubtype.ELEMENTAL),
                                        Set.of(Keyword.HASTE, Keyword.MENACE), null, Set.of(),
                                        Subjects.target(), Duration.UNTIL_END_OF_TURN),
                                "Have the target land become a 3/3 Elemental creature with haste and menace until end of turn?")),
                "+1: Untap target land you control. You may have it become a 3/3 Elemental creature with haste and menace until end of turn. It's still a land.",
                TargetFilters.landYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new PutCardFromHandOrGraveyardOntoBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardManaValueAtMostControlledLandsPredicate())),
                        "creature", CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "−5: You may put a creature card with mana value less than or equal to the number of lands you control onto the battlefield from your hand or graveyard with two +1/+1 counters on it."
        ));
    }
}
