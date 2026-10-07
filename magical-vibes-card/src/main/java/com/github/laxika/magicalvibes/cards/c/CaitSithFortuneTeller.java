package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "FIC", collectorNumber = "54")
@CardRegistration(set = "FIC", collectorNumber = "151")
public class CaitSithFortuneTeller extends Card {

    public CaitSithFortuneTeller() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new ScryEffect(1),
                new ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect()));
    }
}
