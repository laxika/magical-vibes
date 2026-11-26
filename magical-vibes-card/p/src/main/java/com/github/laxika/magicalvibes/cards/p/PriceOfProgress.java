package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "95")
@CardRegistration(set = "PD2", collectorNumber = "18")
@CardRegistration(set = "EMA", collectorNumber = "141")
@CardRegistration(set = "SLD", collectorNumber = "1521")
@CardRegistration(set = "SLZ", collectorNumber = "65")
@CardRegistration(set = "SLZ", collectorNumber = "186")
@CardRegistration(set = "SLZ", collectorNumber = "307")
public class PriceOfProgress extends Card {

    public PriceOfProgress() {
        addSpellEffect(DealDamageEffect.to(Subjects.eachPlayer(), new Scaled(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                                )),
                                CountScope.CONTROLLER
                        ), 2)));
    }
}
