package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "234")
@CardRegistration(set = "DBL", collectorNumber = "234")
@CardRegistration(set = "DSC", collectorNumber = "227")
public class OldStickfingers extends Card {

    public OldStickfingers() {
        addTrigger(Triggers.youCastThisSpell(),
                new RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect(
                        new XValue(), new CardTypePredicate(CardType.CREATURE)));

        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(creatureCards, creatureCards));
    }
}
