package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndStationEffect;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "6")
public class ExperimentalPilot extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Cultivator's Caravan",
            "Bomat Bazaar Barge",
            "Raiders' Karve",
            "Demolition Stomper",
            "Futurist Sentinel",
            "Mechtitan Core",
            "Reckoner Bankbuster",
            "High-Speed Hoverbike",
            "Mindlink Mech",
            "Silent Submersible",
            "Mobile Garrison",
            "Untethered Express",
            "Ovalchase Dragster",
            "Daredevil Dragster",
            "Thundering Chariot");

    public ExperimentalPilot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null, 2),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)),
                "{U}, Discard two cards: Draft a card from Experimental Pilot's spellbook."
        ));

        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new PowerBoostForCrewAndStationEffect(2), GrantScope.SELF));
    }
}
