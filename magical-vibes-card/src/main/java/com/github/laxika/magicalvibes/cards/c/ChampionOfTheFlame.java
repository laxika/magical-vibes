package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "DOM", collectorNumber = "116")
public class ChampionOfTheFlame extends Card {

    public ChampionOfTheFlame() {
        // Champion of the Flame gets +2/+2 for each Aura and Equipment attached to it.
        Scaled twicePerAttachment = new Scaled(new AttachmentsOnSource(true, true), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAttachment, twicePerAttachment));
    }
}
