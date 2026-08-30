package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "226")
public class HermiticHerbalist extends Card {

    public HermiticHerbalist() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        2, ManaSpendRestriction.SUBTYPE_SPELL, Set.of(CardSubtype.LESSON), true)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast Lesson spells."
        ));
    }
}
