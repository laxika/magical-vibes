package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "5")
public class DegaSanctuary extends Card {

    public DegaSanctuary() {
        var controlsBlack = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        var controlsRed = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED)));
        var controlsBlackAndRed = new AllOf(List.of(controlsBlack, controlsRed));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyOf(List.of(controlsBlack, controlsRed))),
                new SequenceEffect(List.of(
                        ConditionalEffect.unless(controlsBlackAndRed, new GainLifeEffect(4)),
                        ConditionalEffect.unless(new NotCondition(controlsBlackAndRed), new GainLifeEffect(2)))));
    }
}
