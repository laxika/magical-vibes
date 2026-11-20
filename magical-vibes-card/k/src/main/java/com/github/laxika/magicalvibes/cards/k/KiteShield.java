package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M12", collectorNumber = "210")
public class KiteShield extends Card {

    public KiteShield() {
        addStaticEffect(new StaticBoostEffect(0, 3, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
