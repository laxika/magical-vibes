package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AVR", collectorNumber = "212")
public class AngelicArmaments extends Card {

    public AngelicArmaments() {
        // Equipped creature gets +2/+2
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        // ... has flying ...
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));
        // ... and is a white Angel in addition to its other colors and types
        addStaticEffect(new GrantColorEffect(CardColor.WHITE, Subjects.attachedCreature()));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.ANGEL, Subjects.attachedCreature()));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
