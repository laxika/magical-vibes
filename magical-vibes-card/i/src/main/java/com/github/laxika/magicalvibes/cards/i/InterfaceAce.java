package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UseToughnessForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "17")
public class InterfaceAce extends Card {

    public InterfaceAce() {
        addStaticEffect(new UseToughnessForCrewAndSaddleEffect());
        addTrigger(Triggers.permanentYouControlBecomesTapped(), new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(), new OncePerTurnTriggerEffect(
                        new ConditionalEffect(new ControllerTurn(),
                                new UntapPermanentsEffect(TapUntapScope.SELF)))));
    }
}
