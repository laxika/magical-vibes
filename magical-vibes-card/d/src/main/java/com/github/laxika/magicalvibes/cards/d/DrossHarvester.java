package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "63")
public class DrossHarvester extends Card {

    public DrossHarvester() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new LoseLifeEffect(4));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new GainLifeEffect(2));
    }
}
