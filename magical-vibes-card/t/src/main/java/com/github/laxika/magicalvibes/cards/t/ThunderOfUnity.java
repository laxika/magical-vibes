package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TDM", collectorNumber = "231")
public class ThunderOfUnity extends Card {

    public ThunderOfUnity() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new LoseLifeEffect(2));

        var creatureEntryDrain = new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_II, creatureEntryDrain);
        addEffect(EffectSlot.SAGA_CHAPTER_III, creatureEntryDrain);
    }
}
