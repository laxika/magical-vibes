package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByChosenPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "98")
@CardRegistration(set = "CM2", collectorNumber = "75")
public class SewerNemesis extends Card {

    public SewerNemesis() {
        addStaticEffect(new ChoosePlayerOnEnterEffect());

        CardsInGraveyard chosenPlayerGraveyard =
                new CardsInGraveyard(null, CountScope.CHOSEN_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(chosenPlayerGraveyard, chosenPlayerGraveyard));

        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryControlledByChosenPlayerPredicate()),
                new MillEffect(1, MillRecipient.CHOSEN_PLAYER));
    }
}
