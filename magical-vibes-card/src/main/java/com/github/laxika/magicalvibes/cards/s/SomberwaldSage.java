package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "215")
@CardRegistration(set = "AVR", collectorNumber = "194")
public class SomberwaldSage extends Card {

    public SomberwaldSage() {
        // {T}: Add three mana of any one color. Spend this mana only to cast creature spells.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3, ManaSpendRestriction.CREATURE_SPELL_ONLY)),
                "{T}: Add three mana of any one color. Spend this mana only to cast creature spells."
        ));
    }
}
