package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "51")
@CardRegistration(set = "LCC", collectorNumber = "83")
public class MasterOfDarkRites extends Card {

    public MasterOfDarkRites() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new AwardRestrictedManaEffect(
                                ManaColor.BLACK,
                                3,
                                new ManaRestriction.SubtypeSpellOnly(Set.of(
                                        CardSubtype.VAMPIRE,
                                        CardSubtype.CLERIC,
                                        CardSubtype.DEMON
                                ))
                        )
                ),
                "{T}, Sacrifice another creature: Add {B}{B}{B}. Spend this mana only to cast Vampire, Cleric, and/or Demon spells."
        ));
    }
}
