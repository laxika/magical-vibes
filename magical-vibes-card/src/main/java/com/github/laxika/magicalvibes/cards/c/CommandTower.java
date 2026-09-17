package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "ANB", collectorNumber = "118")
@CardRegistration(set = "TMC", collectorNumber = "63")
@CardRegistration(set = "REX", collectorNumber = "26")
@CardRegistration(set = "C13", collectorNumber = "281")
@CardRegistration(set = "CMD", collectorNumber = "269")
@CardRegistration(set = "SLZ", collectorNumber = "118")
@CardRegistration(set = "SLZ", collectorNumber = "239")
@CardRegistration(set = "SLZ", collectorNumber = "360")
public class CommandTower extends Card {

    public CommandTower() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)),
                "{T}: Add one mana of any color in your commander's color identity."
        ));
    }
}
