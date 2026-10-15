package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YDFT", collectorNumber = "10")
public class TerrorsOfTheTrack extends Card {

    public TerrorsOfTheTrack() {
        // Whenever this creature or another creature dies, each opponent loses 1 life and you gain
        // 1 life. This ability triggers only once each turn.
        var deathTrigger = new OncePerTurnTriggerEffect(SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));
        addEffect(EffectSlot.ON_DEATH, deathTrigger);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, deathTrigger);
    }
}
