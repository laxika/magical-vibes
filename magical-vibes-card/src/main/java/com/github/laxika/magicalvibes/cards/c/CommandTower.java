package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "677")
@CardRegistration(set = "SLD", collectorNumber = "697")
@CardRegistration(set = "SLD", collectorNumber = "710")
@CardRegistration(set = "SLD", collectorNumber = "744")
@CardRegistration(set = "SLD", collectorNumber = "758")
@CardRegistration(set = "SLD", collectorNumber = "792")
@CardRegistration(set = "SLD", collectorNumber = "806")
@CardRegistration(set = "SLD", collectorNumber = "917")
@CardRegistration(set = "SLD", collectorNumber = "1000")
@CardRegistration(set = "SLD", collectorNumber = "1496")
@CardRegistration(set = "SLD", collectorNumber = "1666")
@CardRegistration(set = "SLD", collectorNumber = "1697")
@CardRegistration(set = "SLD", collectorNumber = "1989")
@CardRegistration(set = "SLD", collectorNumber = "1994")
@CardRegistration(set = "ANB", collectorNumber = "118")
@CardRegistration(set = "TMC", collectorNumber = "63")
@CardRegistration(set = "REX", collectorNumber = "26")
@CardRegistration(set = "C13", collectorNumber = "281")
@CardRegistration(set = "C15", collectorNumber = "281")
@CardRegistration(set = "CMD", collectorNumber = "269")
@CardRegistration(set = "SLZ", collectorNumber = "118")
@CardRegistration(set = "SLZ", collectorNumber = "239")
@CardRegistration(set = "SLZ", collectorNumber = "360")
@CardRegistration(set = "MSC", collectorNumber = "233")
@CardRegistration(set = "MSC", collectorNumber = "234")
@CardRegistration(set = "MSC", collectorNumber = "235")
@CardRegistration(set = "MSC", collectorNumber = "236")
@CardRegistration(set = "ECC", collectorNumber = "59")
@CardRegistration(set = "ECC", collectorNumber = "60")
@CardRegistration(set = "CMM", collectorNumber = "420")
@CardRegistration(set = "CMM", collectorNumber = "659")
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
