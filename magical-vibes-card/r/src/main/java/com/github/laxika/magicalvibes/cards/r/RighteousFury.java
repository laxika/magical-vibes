package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.List;

@CardRegistration(set = "P02", collectorNumber = "21")
@CardRegistration(set = "S99", collectorNumber = "23")
@CardRegistration(set = "ME2", collectorNumber = "30")
@CardRegistration(set = "MAR", collectorNumber = "49")
public class RighteousFury extends Card {

    public RighteousFury() {
        // Destroy all tapped creatures. You gain 2 life for each creature destroyed this way.
        addSpellEffect(new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()))));
        addSpellEffect(new GainLifeEffect(new Scaled(new AmountThisWay(StepKind.DESTROY), 2)));
    }
}
