package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "NEM", collectorNumber = "124")
public class WildMammoth extends Card {

    public WildMammoth() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new APlayerControlsMoreCreaturesThanEachOtherPlayer(),
                new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect()));
    }
}
