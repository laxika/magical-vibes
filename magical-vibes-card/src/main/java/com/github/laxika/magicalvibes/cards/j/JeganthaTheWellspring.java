package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "222")
public class JeganthaTheWellspring extends Card {

    public JeganthaTheWellspring() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.WHITE, 1, new ManaRestriction.ColoredCosts()),
                        new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.ColoredCosts()),
                        new AwardRestrictedManaEffect(ManaColor.BLACK, 1, new ManaRestriction.ColoredCosts()),
                        new AwardRestrictedManaEffect(ManaColor.RED, 1, new ManaRestriction.ColoredCosts()),
                        new AwardRestrictedManaEffect(ManaColor.GREEN, 1, new ManaRestriction.ColoredCosts())
                ),
                "{T}: Add {W}{U}{B}{R}{G}. This mana can't be spent to pay generic mana costs."
        ));
    }
}
