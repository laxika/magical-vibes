package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TMP", collectorNumber = "24")
@CardRegistration(set = "TPR", collectorNumber = "16")
public class Humility extends Card {

    public Humility() {
        // All creatures lose all abilities (layer 6) and have base power and toughness 1/1 (layer 7b).
        addStaticEffect(new LosesAllAbilitiesEffect(Subjects.allCreatures()));
        addStaticEffect(new SetBasePowerToughnessEffect(1, 1, Subjects.allCreatures()));
    }
}
