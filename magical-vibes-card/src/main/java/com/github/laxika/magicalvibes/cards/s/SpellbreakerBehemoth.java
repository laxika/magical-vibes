package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;

@CardRegistration(set = "ARB", collectorNumber = "60")
public class SpellbreakerBehemoth extends Card {

    public SpellbreakerBehemoth() {
        // "This spell can't be countered." — protects its own spell, which the static ability below
        // cannot (that only functions once the Behemoth is a permanent on the battlefield).
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        // "Creature spells you control with power 5 or greater can't be countered."
        addEffect(EffectSlot.STATIC, new ControllerCreatureSpellsCantBeCounteredEffect(5));
    }
}
