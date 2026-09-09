package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "253")
public class UltronDrone extends Card {

    public UltronDrone() {
        CreateTokenEffect robot = new CreateTokenEffect(
                "Robot", 2, 2, null,
                List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(), Set.of(CardType.ARTIFACT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        robot
                ),
                "Power-up — {6}: Put two +1/+1 counters on this creature and create a 2/2 colorless Robot Villain artifact creature token. "
                        + "Activate each power-up ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
