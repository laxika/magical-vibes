package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LeechingLurker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "94")
@CardRegistration(set = "DBL", collectorNumber = "94")
public class CurseOfLeeches extends Card {

    public CurseOfLeeches() {
        setBackFaceCard(new LeechingLurker());
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER),
                new GainLifeEffect(1)));
    }

    @Override
    public String getBackFaceClassName() {
        return "LeechingLurker";
    }
}
