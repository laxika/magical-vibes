package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByChosenPlayerPredicate;

import java.util.List;

@CardRegistration(set = "CMD", collectorNumber = "98")
public class SewerNemesis extends Card {

    public SewerNemesis() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePlayerOnEnterEffect());

        CardsInGraveyard chosenPlayerGraveyard =
                new CardsInGraveyard(null, CountScope.CHOSEN_PLAYER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(chosenPlayerGraveyard, chosenPlayerGraveyard));

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new MillEffect(1, MillRecipient.CHOSEN_PLAYER)),
                        new StackEntryControlledByChosenPlayerPredicate()));
    }
}
