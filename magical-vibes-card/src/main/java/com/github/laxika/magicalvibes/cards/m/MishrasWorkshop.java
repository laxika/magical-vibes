package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "81")
@CardRegistration(set = "VMA", collectorNumber = "305")
public class MishrasWorkshop extends Card {

    public MishrasWorkshop() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 3, new ManaRestriction.ArtifactSpellsOnly())),
                "{T}: Add {C}{C}{C}. Spend this mana only to cast artifact spells."
        ));
    }
}
