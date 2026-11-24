package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureWithHitCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "89")
@CardRegistration(set = "NCC", collectorNumber = "97")
@CardRegistration(set = "OTC", collectorNumber = "138")
public class MariTheKillingQuill extends Card {

    public MariTheKillingQuill() {
        var outlaw = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.ASSASSIN, CardSubtype.MERCENARY, CardSubtype.ROGUE));

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new ExileTriggeringCreatureWithHitCounterEffect());
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.creaturesYouControl(), outlaw));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new MayEffect(SequenceEffect.of(
                                ChooseCardEffect.oneOf(Subjects.exiledCards(Subjects.triggeringPlayer(),
                                                new CardHasCounterInExilePredicate(CounterType.HIT)),
                                        "a card that player owns in exile with a hit counter on it"),
                                new RemoveCountersFromExiledCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                                        CounterType.HIT),
                                new ConditionalEffect(HappenedThisWay.removedCounters(),
                                        SequenceEffect.of(new DrawCardEffect(1),
                                                CreateTokenEffect.ofTreasureToken(2)))),
                                "Remove a hit counter from a card that player owns in exile?")),
                        Subjects.creaturesYouControl(), outlaw));
    }
}
