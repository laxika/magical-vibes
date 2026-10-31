package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "FRF", collectorNumber = "55")
@CardRegistration(set = "SLD", collectorNumber = "2190")
@CardRegistration(set = "PIO", collectorNumber = "77")
@CardRegistration(set = "ACR", collectorNumber = "86")
public class TemporalTrespass extends Card {

    public TemporalTrespass() {
        addCastingCost(new DelveCost());
        addSpellEffect(new ControllerExtraTurnEffect(1));
        addSpellEffect(new ExileSpellEffect());
    }
}
