package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceFightsRandomOpponentCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "101")
@CardRegistration(set = "2X2", collectorNumber = "272")
public class ScabClanGiant extends Card {

    public ScabClanGiant() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SourceFightsRandomOpponentCreatureEffect());
    }
}
