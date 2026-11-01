package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "86")
@CardRegistration(set = "LCI", collectorNumber = "413")
public class ZoeticGlyph extends Card {

    public ZoeticGlyph() {
        target(TargetFilters.artifact())
                .addStaticEffect(new GrantCardTypeEffect(CardType.CREATURE, GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.GOLEM, GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new SetBasePowerToughnessEffect(5, 4, GrantScope.ENCHANTED_PERMANENT));

        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), new DiscoverEffect(3));
    }
}
