package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "171")
public class BarrensteppeSiege extends Card {

    public BarrensteppeSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of("Abzan", "Mardu")));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode("Abzan")),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new SourceHasChosenMode("Mardu"),
                                new CreatureDiedUnderYourControlThisTurn()))),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
    }
}
