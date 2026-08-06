package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

/** Back face of {@link com.github.laxika.magicalvibes.cards.c.CryptolithFragment}. */
public class AuroraOfEmrakul extends Card {

    public AuroraOfEmrakul() {
        // Whenever this creature attacks, each opponent loses 3 life.
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT));
    }
}
