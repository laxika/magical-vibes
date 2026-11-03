package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "137")
public class QuillSlingerBoggart extends Card {

    public QuillSlingerBoggart() {
        // Whenever a player casts a Kithkin spell, you may have target player lose 1 life.
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardSubtypePredicate(CardSubtype.KITHKIN)),
                new MayEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), "Have target player lose 1 life?"));
    }
}
