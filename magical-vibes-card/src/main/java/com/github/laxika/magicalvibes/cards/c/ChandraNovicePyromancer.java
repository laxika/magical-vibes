package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "128")
public class ChandraNovicePyromancer extends Card {

    public ChandraNovicePyromancer() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new BoostAllOwnCreaturesEffect(
                        2,
                        0,
                        new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL))),
                "+1: Elementals you control get +2/+0 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new AwardManaEffect(ManaColor.RED, 2)),
                "\u22121: Add {R}{R}."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DealDamageToAnyTargetEffect(2)),
                "\u22122: Chandra, Novice Pyromancer deals 2 damage to any target."
        ));
    }
}
