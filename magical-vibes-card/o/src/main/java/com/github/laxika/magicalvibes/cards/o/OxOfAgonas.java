package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceWasCastWithEscape;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "147")
@CardRegistration(set = "THB", collectorNumber = "318")
@CardRegistration(set = "NEC", collectorNumber = "108")
public class OxOfAgonas extends Card {

    public OxOfAgonas() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DiscardHandEffect(),
                new DrawCardEffect(3)));

        addCastingOption(new GraveyardCast(null, "{R}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 8)),
                null, false, false, true));

        addStaticEffect(new ConditionalEffect(
                new SourceWasCastWithEscape(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
