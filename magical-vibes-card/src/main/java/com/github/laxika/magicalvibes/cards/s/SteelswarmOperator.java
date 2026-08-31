package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "80")
public class SteelswarmOperator extends Card {

    public SteelswarmOperator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 1, new ManaRestriction.ArtifactSpellsOnly())),
                "{T}: Add {U}. Spend this mana only to cast an artifact spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 2, new ManaRestriction.ArtifactAbilities())),
                "{T}: Add {U}{U}. Spend this mana only to activate abilities of artifact sources."
        ));
    }
}
