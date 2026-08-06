package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "101")
public class VaporousDjinn extends Card {

    public VaporousDjinn() {
        // "At the beginning of your upkeep, this creature phases out unless you pay {U}{U}."
        // Flying is a printed keyword loaded from Scryfall.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}{U}"),
                        List.of(new PhaseOutEffect(PhaseOutSubject.SOURCE)),
                        true));
    }
}
