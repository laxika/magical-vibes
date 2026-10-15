package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "139")
public class Flamebraider extends Card {

    public Flamebraider() {
        // {T}: Add two mana in any combination of colors. Spend this mana only to cast Elemental spells or activate abilities of Elementals.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY, CardSubtype.ELEMENTAL)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast Elemental spells or activate abilities of Elementals."
        ));
    }
}
