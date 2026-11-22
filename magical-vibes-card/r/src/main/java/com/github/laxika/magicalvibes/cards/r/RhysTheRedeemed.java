package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "237")
@CardRegistration(set = "2XM", collectorNumber = "213")
@CardRegistration(set = "TLE", collectorNumber = "52")
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
                List.of(new CreateTokenCopyOfTargetPermanentEffect().withSubject(Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsTokenPredicate(),
                                new PermanentIsCreaturePredicate()))))),
                "{4}{G/W}{G/W}, {T}: For each creature token you control, create a token that's a copy of that creature."
        ));
    }
}
