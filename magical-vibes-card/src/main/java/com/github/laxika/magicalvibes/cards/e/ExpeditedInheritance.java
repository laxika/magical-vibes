package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MKM", collectorNumber = "123")
public class ExpeditedInheritance extends Card {

    public ExpeditedInheritance() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, new MayEffect(
                ExileTopCardsMayPlayUntilNextTurnEffect.forTriggeringPermanentController(new EventValue()),
                "Exile that many cards from the top of your library?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
