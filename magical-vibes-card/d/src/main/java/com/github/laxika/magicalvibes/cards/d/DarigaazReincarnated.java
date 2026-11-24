package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExileWithCounter;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "193")
public class DarigaazReincarnated extends Card {

    public DarigaazReincarnated() {
        addStaticEffect(new ExileWithEggCountersInsteadOfDyingEffect(3));
        var withEggCounter = new SourceCardInExileWithCounter(CounterType.EGG);
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                TriggerOptions.DEFAULT.withInterveningIf(withEggCounter),
                SequenceEffect.of(
                        new RemoveCountersFromExiledCardsEffect(Subjects.sourceCard(), CounterType.EGG),
                        new ConditionalEffect(new NotCondition(withEggCounter),
                                new PutCardsOntoBattlefieldEffect(Subjects.sourceCard()))));
    }
}
