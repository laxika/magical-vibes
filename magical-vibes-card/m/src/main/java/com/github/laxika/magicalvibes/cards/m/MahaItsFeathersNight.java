package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "BLB", collectorNumber = "100")
public class MahaItsFeathersNight extends Card {

    public MahaItsFeathersNight() {
        // Ward—Discard a card. (Whenever this creature becomes the target of a spell or ability
        // an opponent controls, counter it unless that player discards a card.)
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessDiscardsEffect());

        addStaticEffect(new SetBasePowerToughnessEffect(null, 1, GrantScope.OPPONENT_CREATURES));
    }
}
