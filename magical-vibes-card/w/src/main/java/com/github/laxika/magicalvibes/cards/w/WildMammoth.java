package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "124")
public class WildMammoth extends Card {

    public WildMammoth() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new APlayerControlsMoreCreaturesThanEachOtherPlayer(),
                new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect()));
    }
}
