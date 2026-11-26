package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "HOU", collectorNumber = "58")
public class ApocalypseDemon extends Card {

    public ApocalypseDemon() {
        // Flying is auto-loaded from Scryfall.

        // Apocalypse Demon's power and toughness are each equal to the number of cards in your graveyard.
        CardsInGraveyard cardsInYourGraveyard = new CardsInGraveyard(null, CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(cardsInYourGraveyard, cardsInYourGraveyard));

        // At the beginning of your upkeep, tap this creature unless you sacrifice another creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay(new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "another creature"), new TapPermanentsEffect(Subjects.source())));
    }
}
