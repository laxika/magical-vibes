package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "129")
public class ChandrasEmbercat extends Card {

    public ChandrasEmbercat() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.RED,
                        1,
                        new ManaRestriction.SubtypeOrPlaneswalkerSpells(CardSubtype.ELEMENTAL, CardSubtype.CHANDRA)
                )),
                "{T}: Add {R}. Spend this mana only to cast an Elemental spell or a Chandra planeswalker spell."
        ));
    }
}
