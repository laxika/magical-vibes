package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetCreaturesCantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "224")
public class HuatliWarriorPoet extends Card {

    public HuatliWarriorPoet() {
        // +2: You gain life equal to the greatest power among creatures you control.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect()),
                "+2: You gain life equal to the greatest power among creatures you control."
        ));

        // 0: Create a 3/3 green Dinosaur creature token with trample.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        "Dinosaur", 3, 3,
                        CardColor.GREEN,
                        List.of(CardSubtype.DINOSAUR),
                        Set.of(Keyword.TRAMPLE),
                        Set.of()
                )),
                "0: Create a 3/3 green Dinosaur creature token with trample."
        ));

        // −X: Huatli, Warrior Poet deals X damage divided as you choose among any number
        // of target creatures. Creatures dealt damage this way can't block this turn.
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new DealXDamageDividedAmongTargetCreaturesCantBlockEffect()),
                "\u2212X: Huatli, Warrior Poet deals X damage divided as you choose among any number of target creatures. Creatures dealt damage this way can't block this turn.",
                null
        ));
    }
}
