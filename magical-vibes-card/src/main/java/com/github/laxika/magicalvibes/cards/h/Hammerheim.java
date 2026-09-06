package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RemoveAllLandwalkAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "302")
public class Hammerheim extends Card {

    public Hammerheim() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RemoveAllLandwalkAbilitiesEffect()),
                "{T}: Target creature loses all landwalk abilities until end of turn.",
                TargetFilters.creature()
        ));
    }
}
