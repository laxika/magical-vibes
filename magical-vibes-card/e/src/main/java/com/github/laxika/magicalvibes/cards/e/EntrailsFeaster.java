package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "143")
public class EntrailsFeaster extends Card {

    public EntrailsFeaster() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                ExileUpToOneCardFromGraveyardEffect.matching(new CardTypePredicate(CardType.CREATURE)),
                new ConditionalEffect(HappenedThisWay.exiled(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                new ConditionalEffect(new NotCondition(HappenedThisWay.exiled()),
                        new TapPermanentsEffect(Subjects.source()))));
    }
}
