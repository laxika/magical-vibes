package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HML", collectorNumber = "79")
public class Retribution extends Card {

    public Retribution() {
        // "Choose two target creatures controlled by the same opponent. That player chooses and
        // sacrifices one of those creatures. Put a -1/-1 counter on the other."
        // This engine is strictly 2-player, so "an opponent controls" on both target groups already
        // means the same opponent controls both.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect());
        target(TargetFilters.creatureAnOpponentControls());
    }
}
