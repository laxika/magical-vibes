package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ORI", collectorNumber = "104")
public class KothophedSoulHoarder extends Card {

    public KothophedSoulHoarder() {
        // Whenever a permanent owned by another player is put into a graveyard from the battlefield,
        // you draw a card and you lose 1 life.
        addEffect(EffectSlot.ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
