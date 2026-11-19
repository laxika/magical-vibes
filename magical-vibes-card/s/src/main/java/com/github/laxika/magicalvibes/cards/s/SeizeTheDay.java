package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "220")
@CardRegistration(set = "SLD", collectorNumber = "480")
@CardRegistration(set = "SLD", collectorNumber = "1172")
@CardRegistration(set = "UMA", collectorNumber = "147")
@CardRegistration(set = "MSC", collectorNumber = "166")
@CardRegistration(set = "MSC", collectorNumber = "371")
@CardRegistration(set = "MAR", collectorNumber = "73")
public class SeizeTheDay extends Card {

    public SeizeTheDay() {
        target(TargetFilters.creature())
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new AdditionalCombatMainPhaseEffect(1, null, true));
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
