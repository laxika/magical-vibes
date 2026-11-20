package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "11")
public class MysticFamiliar extends Card {

    public MysticFamiliar() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(1, 1, Subjects.source())));
        addStaticEffect(new ConditionalEffect(threshold,
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK))));
    }
}
