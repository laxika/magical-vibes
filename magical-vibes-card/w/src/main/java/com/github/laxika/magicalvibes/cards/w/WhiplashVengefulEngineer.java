package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSH", collectorNumber = "121")
public class WhiplashVengefulEngineer extends Card {

    public WhiplashVengefulEngineer() {
        AttachmentsOnSource equipmentCount = new AttachmentsOnSource(false, true);
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new Equipped(),
                SequenceEffect.of(
                        new LoseLifeEffect(equipmentCount, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(equipmentCount))));
    }
}
