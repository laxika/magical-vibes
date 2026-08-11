package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "106")
public class LegionsEnd extends Card {

    public LegionsEnd() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffect());
    }
}
