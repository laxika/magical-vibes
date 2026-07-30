package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;

@CardRegistration(set = "M12", collectorNumber = "192")
public class RitesOfFlourishing extends Card {

    public RitesOfFlourishing() {
        // At the beginning of each player's draw step, that player draws an additional card.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1));

        // Each player may play an additional land on each of their turns.
        addEffect(EffectSlot.STATIC, new EachPlayerPlaysAdditionalLandEffect());
    }
}
