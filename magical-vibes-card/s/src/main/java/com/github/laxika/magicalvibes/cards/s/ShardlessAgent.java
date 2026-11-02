package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PC2", collectorNumber = "104")
@CardRegistration(set = "EMA", collectorNumber = "206")
@CardRegistration(set = "PCA", collectorNumber = "104")
@CardRegistration(set = "MH2", collectorNumber = "292")
public class ShardlessAgent extends Card {

    public ShardlessAgent() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
