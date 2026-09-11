package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "57")
public class HydraulicHelper extends Card {

    public HydraulicHelper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 1, new ManaRestriction.ArtifactSpellsOrAbilities())),
                "{T}: Add {U}. This mana can't be spent to cast a nonartifact spell."
        ));
    }
}
