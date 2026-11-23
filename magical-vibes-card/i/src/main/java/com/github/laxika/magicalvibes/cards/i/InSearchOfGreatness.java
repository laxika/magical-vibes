package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "177")
@CardRegistration(set = "KHM", collectorNumber = "364")
public class InSearchOfGreatness extends Card {

    public InSearchOfGreatness() {
        var greatestOtherManaValue = new GreatestManaValueAmongControlled(new PermanentTruePredicate(), true);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardManaValueAtMostAmountPredicate(greatestOtherManaValue)))),
                new Sum(greatestOtherManaValue, new Fixed(1)))
                .orElse(new ScryEffect(1)));
    }
}
