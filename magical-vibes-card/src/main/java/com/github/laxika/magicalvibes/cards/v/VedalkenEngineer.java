package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "35")
public class VedalkenEngineer extends Card {

    public VedalkenEngineer() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.ARTIFACT_SPELLS_OR_ABILITIES)),
                "{T}: Add two mana of any one color. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));
    }
}
