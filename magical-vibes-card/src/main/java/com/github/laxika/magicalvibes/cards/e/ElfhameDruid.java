package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "159")
public class ElfhameDruid extends Card {

    public ElfhameDruid() {
        // {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        // {T}: Add {G}{G}. Spend this mana only to cast kicked spells.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.GREEN, 2, new ManaRestriction.KickedCosts())),
                "{T}: Add {G}{G}. Spend this mana only to cast kicked spells."
        ));
    }
}
