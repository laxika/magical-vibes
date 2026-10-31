package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "199")
@CardRegistration(set = "CP1", collectorNumber = "5")
public class ProphetOfKruphix extends Card {

    public ProphetOfKruphix() {
        addStaticEffect(new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()))));
        addStaticEffect(new GrantFlashToCardTypeEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
