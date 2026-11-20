package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "57")
@CardRegistration(set = "DMR", collectorNumber = "63")
public class Ovinize extends Card {

    public Ovinize() {
        target(TargetFilters.creature())
                .addSpellEffect(new LosesAllAbilitiesEffect(Subjects.target(), Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new SetBasePowerToughnessEffect(0, 1));
    }
}
