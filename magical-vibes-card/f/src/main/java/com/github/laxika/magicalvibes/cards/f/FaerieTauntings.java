package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "112")
public class FaerieTauntings extends Card {

    public FaerieTauntings() {
        // Whenever you cast a spell during an opponent's turn, you may have each opponent lose 1 life.
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn(),
                new MayEffect(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT), "Have each opponent lose 1 life?"));
    }
}
