package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "133")
@CardRegistration(set = "INR", collectorNumber = "322")
@CardRegistration(set = "INR", collectorNumber = "476")
@CardRegistration(set = "M20", collectorNumber = "115")
@CardRegistration(set = "SLD", collectorNumber = "1244")
public class SorinImperiousBloodlord extends Card {

    public SorinImperiousBloodlord() {
        // +1: Target creature you control gains deathtouch and lifelink until end of turn.
        // If it's a Vampire, put a +1/+1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new GrantKeywordEffect(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), Subjects.target()),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)),
                                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                ),
                "+1: Target creature you control gains deathtouch and lifelink until end of turn. "
                        + "If it's a Vampire, put a +1/+1 counter on it.",
                TargetFilters.creatureYouControl()
        ));

        // +1: You may sacrifice a Vampire. When you do, Sorin deals 3 damage to any target and you gain 3 life.
        // The reflexive ability chooses its any-target as it goes on the stack.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                        SequenceEffect.of(
                                        DealDamageEffect.toAnyTarget(3),
                                        new GainLifeEffect(3))))),
                        "Sacrifice a Vampire?")),
                "+1: You may sacrifice a Vampire. When you do, Sorin deals 3 damage to any target and you gain 3 life."
        ));

        // −3: You may put a Vampire creature card from your hand onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.VAMPIRE))),
                                "Vampire creature"),
                        "Put a Vampire creature card from your hand onto the battlefield?")),
                "\u22123: You may put a Vampire creature card from your hand onto the battlefield."
        ));
    }
}
