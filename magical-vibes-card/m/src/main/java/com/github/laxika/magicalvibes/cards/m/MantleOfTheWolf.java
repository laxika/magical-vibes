package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "178")
@CardRegistration(set = "THB", collectorNumber = "327")
public class MantleOfTheWolf extends Card {

    public MantleOfTheWolf() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                        new CreateTokenEffect(2, "Wolf", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.WOLF), Set.of(), Set.of()));
    }
}
