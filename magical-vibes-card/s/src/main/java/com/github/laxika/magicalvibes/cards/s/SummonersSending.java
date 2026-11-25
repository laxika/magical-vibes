package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "29")
@CardRegistration(set = "FIC", collectorNumber = "109")
public class SummonersSending extends Card {

    public SummonersSending() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.CREATURE), false),
                        new ConditionalEffect(HappenedThisWay.exiled(), CreateTokenEffect.whiteSpirit(1)),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardMinManaValuePredicate(4)),
                                new PutCountersEffect(Subjects.createdThisResolution(),
                                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)))),
                "Exile target creature card from a graveyard?"));
    }
}
