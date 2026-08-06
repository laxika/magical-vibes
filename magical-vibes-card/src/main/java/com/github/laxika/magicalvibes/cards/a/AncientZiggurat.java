package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "141")
public class AncientZiggurat extends Card {

    public AncientZiggurat() {
        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CREATURE_SPELL_ONLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell."
        ));
    }
}
