package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "142")
public class GodheadOfAwe extends Card {

    public GodheadOfAwe() {
        // Other creatures have base power and toughness 1/1 (the source is excluded by the
        // base-P/T layered pass, so ALL_CREATURES == "other creatures" here).
        addStaticEffect(new SetBasePowerToughnessEffect(1, 1, Subjects.otherCreatures()));
    }
}
