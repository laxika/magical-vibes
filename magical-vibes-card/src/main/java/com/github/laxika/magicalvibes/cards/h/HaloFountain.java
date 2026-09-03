package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "15")
public class HaloFountain extends Card {

    public HaloFountain() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new UntapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))),
                "{W}, {T}, Untap a tapped creature you control: Create a 1/1 green and white Citizen creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(
                        new UntapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect()),
                "{W}{W}, {T}, Untap two tapped creatures you control: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}{W}{W}{W}",
                List.of(
                        new UntapMultiplePermanentsCost(15, new PermanentIsCreaturePredicate()),
                        new WinGameEffect()),
                "{W}{W}{W}{W}{W}, {T}, Untap fifteen tapped creatures you control: You win the game."
        ));
    }
}
