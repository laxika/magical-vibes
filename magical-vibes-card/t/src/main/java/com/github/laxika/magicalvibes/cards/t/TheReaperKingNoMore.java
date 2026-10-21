package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECC", collectorNumber = "4")
public class TheReaperKingNoMore extends Card {

    public TheReaperKingNoMore() {
        // When this creature enters, put a -1/-1 counter on each of up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Whenever a creature an opponent controls with a -1/-1 counter on it dies, you may put
        // that card onto the battlefield under your control. Do this only once each turn. The
        // trigger always goes on the stack; the once-per-turn limit is used only when the return is
        // accepted on resolution (CR 603.5).
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, OncePerTurnTriggerEffect.markOnAcceptance(
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                        new ReturnDyingOpponentCreatureUnderYourControlEffect())));
    }
}
