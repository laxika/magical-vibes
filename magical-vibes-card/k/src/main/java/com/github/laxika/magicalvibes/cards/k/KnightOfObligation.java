package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "18")
public class KnightOfObligation extends Card {

    public KnightOfObligation() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                new MayPayManaEffect("{W/B}", new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true), "Pay {W/B} to extort?"));
    }
}
