package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachControlledCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "237")
public class RhysTheRedeemed extends Card {

    public RhysTheRedeemed() {
        // {2}{G/W}, {T}: Create a 1/1 green and white Elf Warrior creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G/W}",
                List.of(new CreateTokenEffect(1, "Elf Warrior", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR))),
                "{2}{G/W}, {T}: Create a 1/1 green and white Elf Warrior creature token."
        ));

        // {4}{G/W}{G/W}, {T}: For each creature token you control, create a token that's a copy of that creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G/W}{G/W}",
                List.of(new CreateTokenCopyOfEachControlledCreatureTokenEffect()),
                "{4}{G/W}{G/W}, {T}: For each creature token you control, create a token that's a copy of that creature."
        ));
    }
}
