package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRK", collectorNumber = "50")
public class NamelessRace extends Card {

    public NamelessRace() {
        Sum maximumLifePayment = new Sum(
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                )), CountScope.OPPONENTS),
                new CardsInGraveyard(new CardColorPredicate(CardColor.WHITE), CountScope.OPPONENTS));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayAnyAmountOfLifeOnEnterEffect(maximumLifePayment));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new ChosenNumberOnSource(), new ChosenNumberOnSource()));
    }
}
