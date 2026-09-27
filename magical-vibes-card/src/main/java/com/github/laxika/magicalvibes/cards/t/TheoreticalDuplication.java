package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "C21", collectorNumber = "34")
public class TheoreticalDuplication extends Card {

    public TheoreticalDuplication() {
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenCopyOfEnteringPermanentEffect())));
    }
}
