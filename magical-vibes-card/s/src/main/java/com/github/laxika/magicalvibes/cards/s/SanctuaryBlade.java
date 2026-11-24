package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AsThisBecomesAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "C20", collectorNumber = "69")
public class SanctuaryBlade extends Card {

    public SanctuaryBlade() {
        addStaticEffect(new AsThisBecomesAttachedEffect(new ChooseColorForSourceEffect()));
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(ProtectionFromColorsEffect.chosenColor(Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
