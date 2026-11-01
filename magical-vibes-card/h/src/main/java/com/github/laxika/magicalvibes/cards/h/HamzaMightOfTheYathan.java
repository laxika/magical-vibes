package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCreatureAndManifestEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "21")
public class HamzaMightOfTheYathan extends Card {

    public HamzaMightOfTheYathan() {
        addTrigger(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                EndureEffect.forTriggeringPermanent(new TriggeringPermanentToughness()));

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new SeekCreatureAndManifestEffect());
    }
}
