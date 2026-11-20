package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "247")
public class BlightSickle extends Card {

    public BlightSickle() {
        // Equipped creature gets +1/+0 and has wither.
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.WITHER, Subjects.attachedCreature()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
