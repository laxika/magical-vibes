package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DFT", collectorNumber = "201")
public class EmbalmedAscendant extends Card {

    public EmbalmedAscendant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.blackZombie(1));

        CardEffect maxSpeedDrain = new ConditionalEffect(
                new MaxSpeed(),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, maxSpeedDrain);
        addEffect(EffectSlot.ON_DEATH, maxSpeedDrain);
    }
}
