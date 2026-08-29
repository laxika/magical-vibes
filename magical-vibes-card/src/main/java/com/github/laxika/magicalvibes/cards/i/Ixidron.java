package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOtherNontokenCreaturesFaceDownOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "65")
public class Ixidron extends Card {

    public Ixidron() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TurnOtherNontokenCreaturesFaceDownOnEnterEffect());

        PermanentAllOfPredicate faceDownCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsFaceDownPredicate()));
        PermanentCount faceDownCreatureCount = new PermanentCount(faceDownCreatures, CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(faceDownCreatureCount, faceDownCreatureCount));
    }
}
