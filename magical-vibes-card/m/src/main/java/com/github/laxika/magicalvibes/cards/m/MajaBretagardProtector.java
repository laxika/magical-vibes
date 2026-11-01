package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "222")
@CardRegistration(set = "KHM", collectorNumber = "327")
@CardRegistration(set = "SCD", collectorNumber = "235")
public class MajaBretagardProtector extends Card {

    public MajaBretagardProtector() {
        // Other creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));

        // Landfall — Whenever a land you control enters, create a 1/1 white Human Warrior creature token.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Human Warrior", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR),
                        Set.of(), Set.of()));
    }
}
