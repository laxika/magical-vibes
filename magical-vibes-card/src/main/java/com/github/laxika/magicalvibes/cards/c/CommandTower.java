package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

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
public class CommandTower extends Card {

    public CommandTower() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forCommanderColorIdentity()),
                "{T}: Add one mana of any color in your commander's color identity."
        ));
    }
}
