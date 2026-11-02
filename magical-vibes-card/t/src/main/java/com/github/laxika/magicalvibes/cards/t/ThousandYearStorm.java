package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachPriorInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "248")
@CardRegistration(set = "GRN", collectorNumber = "207")
@CardRegistration(set = "2X2", collectorNumber = "286")
public class ThousandYearStorm extends Card {

    public ThousandYearStorm() {
        addTrigger(Triggers.youCastSpell(), new CopySpellForEachPriorInstantOrSorceryEffect());
    }
}
