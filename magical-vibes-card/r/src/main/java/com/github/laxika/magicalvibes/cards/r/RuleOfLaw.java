package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "37")
@CardRegistration(set = "MRD", collectorNumber = "19")
@CardRegistration(set = "M20", collectorNumber = "35")
@CardRegistration(set = "SLD", collectorNumber = "2478")
public class RuleOfLaw extends Card {

    public RuleOfLaw() {
        addStaticEffect(new LimitSpellsPerTurnEffect(1, SpellLimitScope.EACH_PLAYER));
    }
}
