package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "182")
public class EshkiDragonclaw extends Card {

    public EshkiDragonclaw() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                        new ControllerCastSpellThisTurn(new CardTypePredicate(CardType.CREATURE)),
                                        new ControllerCastSpellThisTurn(
                                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                                ))),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
    }
}
