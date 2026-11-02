package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of {@link com.github.laxika.magicalvibes.cards.c.CryptolithFragment}. */
public class AuroraOfEmrakul extends Card {

    public AuroraOfEmrakul() {
        // Whenever this creature attacks, each opponent loses 3 life.
        addTrigger(Triggers.attacks(), new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT));
    }
}
