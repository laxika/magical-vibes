package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardHasteGrantingManaEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "534")
public class CarnelianOrbOfDragonkind extends Card {

    public CarnelianOrbOfDragonkind() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardHasteGrantingManaEffect(ManaColor.RED, 1, CardSubtype.DRAGON)),
                "{T}: Add {R}. If that mana is spent on a Dragon creature spell, it gains haste until end of turn."
        ));
    }
}
