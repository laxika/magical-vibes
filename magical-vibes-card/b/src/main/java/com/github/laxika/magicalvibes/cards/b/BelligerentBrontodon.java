package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "XLN", collectorNumber = "218")
public class BelligerentBrontodon extends Card {

    public BelligerentBrontodon() {
        addStaticEffect(new AssignCombatDamageWithToughnessEffect(Subjects.creaturesYouControl()));
    }
}
