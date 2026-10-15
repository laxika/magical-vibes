package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "119")
public class AccomplishedAlchemist extends Card {

    public AccomplishedAlchemist() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {T}: Add X mana of any one color, where X is the amount of life you gained this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        new LifeGainedThisTurn(CountScope.CONTROLLER))),
                "{T}: Add X mana of any one color, where X is the amount of life you gained this turn."
        ));
    }
}
