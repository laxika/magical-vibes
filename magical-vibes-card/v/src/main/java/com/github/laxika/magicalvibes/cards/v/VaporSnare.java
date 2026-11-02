package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "44")
public class VaporSnare extends Card {

    public VaporSnare() {
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.LAND));
    }
}
