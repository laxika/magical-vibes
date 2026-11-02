package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantHarmonizeToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "225")
public class SongcrafterMage extends Card {

    public SongcrafterMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GrantHarmonizeToTargetGraveyardCardEffect());
    }
}
