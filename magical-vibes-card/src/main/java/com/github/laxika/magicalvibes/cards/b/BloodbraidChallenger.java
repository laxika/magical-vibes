package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "70")
@CardRegistration(set = "M3C", collectorNumber = "122")
public class BloodbraidChallenger extends Card {

    public BloodbraidChallenger() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addCastingOption(new GraveyardCast(null, "{3}{R}{G}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 3)), null, false, false, true));
    }
}
