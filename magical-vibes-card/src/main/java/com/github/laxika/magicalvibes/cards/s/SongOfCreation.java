package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "210")
public class SongOfCreation extends Card {

    public SongOfCreation() {
        // You may play an additional land on each of your turns.
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        // Whenever you cast a spell, draw two cards.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect(2))));

        // At the beginning of your end step, discard your hand.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscardHandEffect());
    }
}
