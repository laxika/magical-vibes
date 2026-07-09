package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorSubtypeSpellOrAbilityManaEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "189")
public class Smokebraider extends Card {

    public Smokebraider() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorSubtypeSpellOrAbilityManaEffect(2, CardSubtype.ELEMENTAL)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast Elemental spells or activate abilities of Elementals."
        ));
    }
}
