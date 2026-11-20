package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "1")
public class AbsoluteGrace extends Card {

    public AbsoluteGrace() {
        addStaticEffect(new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)), Subjects.allCreatures()));
    }
}
