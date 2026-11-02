package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "165")
@CardRegistration(set = "HBG", collectorNumber = "193")
public class ValorSinger extends Card {

    public ValorSinger() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(1, 0));
    }
}
