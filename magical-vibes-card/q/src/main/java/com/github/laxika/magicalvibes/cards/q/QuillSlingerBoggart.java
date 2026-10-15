package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "137")
public class QuillSlingerBoggart extends Card {

    public QuillSlingerBoggart() {
        // Whenever a player casts a Kithkin spell, you may have target player lose 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.KITHKIN),
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER))
                ),
                "Have target player lose 1 life?"
        ));
    }
}
