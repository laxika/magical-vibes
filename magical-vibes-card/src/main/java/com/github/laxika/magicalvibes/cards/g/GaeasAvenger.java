package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "33")
public class GaeasAvenger extends Card {

    public GaeasAvenger() {
        PermanentCount opponentArtifacts =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS);
        Sum powerAndToughness = new Sum(new Fixed(1), opponentArtifacts);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(powerAndToughness, powerAndToughness));
    }
}
