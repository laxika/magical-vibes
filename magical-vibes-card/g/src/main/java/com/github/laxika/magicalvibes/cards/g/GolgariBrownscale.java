package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "166")
@CardRegistration(set = "UMA", collectorNumber = "166")
public class GolgariBrownscale extends Card {

    public GolgariBrownscale() {
        addTrigger(new TriggerCondition(Triggers.putIntoHandFrom(Zone.GRAVEYARD, null,
                ObjectRelation.THIS), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new GainLifeEffect(2));
        addStaticEffect(new DredgeEffect(2));
    }
}
