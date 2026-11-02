package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "54")
@CardRegistration(set = "FIC", collectorNumber = "151")
public class CaitSithFortuneTeller extends Card {

    public CaitSithFortuneTeller() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new ScryEffect(1),
                new ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect()));
    }
}
