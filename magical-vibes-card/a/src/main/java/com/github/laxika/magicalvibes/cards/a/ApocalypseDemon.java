package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "58")
public class ApocalypseDemon extends Card {

    public ApocalypseDemon() {
        // Flying is auto-loaded from Scryfall.

        // Apocalypse Demon's power and toughness are each equal to the number of cards in your graveyard.
        CardsInGraveyard cardsInYourGraveyard = new CardsInGraveyard(null, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInYourGraveyard, cardsInYourGraveyard));

        // At the beginning of your upkeep, tap this creature unless you sacrifice another creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice another creature"),
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF)),
                        true));
    }
}
