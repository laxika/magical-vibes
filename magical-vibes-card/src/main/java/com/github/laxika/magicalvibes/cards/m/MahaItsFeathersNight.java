package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "BLB", collectorNumber = "100")
public class MahaItsFeathersNight extends Card {

    public MahaItsFeathersNight() {
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(null, 1, GrantScope.OPPONENT_CREATURES));
    }
}
