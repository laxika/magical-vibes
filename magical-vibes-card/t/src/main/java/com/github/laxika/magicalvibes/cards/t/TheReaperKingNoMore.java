package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECC", collectorNumber = "4")
public class TheReaperKingNoMore extends Card {

    public TheReaperKingNoMore() {
        // When this creature enters, put a -1/-1 counter on each of up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Whenever a creature an opponent controls with a -1/-1 counter on it dies, you may put
        // that card onto the battlefield under your control. Do this only once each turn. The
        // trigger always goes on the stack; the once-per-turn limit is used only when the return is
        // accepted on resolution (CR 603.5).
        addTrigger(Triggers.creatureDies(
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                        ObjectRelation.OPPONENT_CONTROLS),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(),
                        "Put that card onto the battlefield under your control?"));
    }
}
