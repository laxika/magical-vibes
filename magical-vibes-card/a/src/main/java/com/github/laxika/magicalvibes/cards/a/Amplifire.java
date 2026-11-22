package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "92")
public class Amplifire extends Card {

    public Amplifire() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RevealCardsFromTopUntilEffect(creatureCard),
                new SetBasePowerToughnessToAmountEffect(
                        new Scaled(AmountThisWay.revealedPower(creatureCard).keepingNegative(), 2),
                        new Scaled(AmountThisWay.revealedToughness(creatureCard).keepingNegative(), 2),
                        Subjects.source(), Duration.UNTIL_YOUR_NEXT_TURN),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
