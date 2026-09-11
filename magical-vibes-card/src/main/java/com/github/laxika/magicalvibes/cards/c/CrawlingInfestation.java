package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "193")
public class CrawlingInfestation extends Card {

    public CrawlingInfestation() {
        // At the beginning of your upkeep, you may mill two cards.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));

        // Whenever one or more creature cards are put into your graveyard from anywhere during your
        // turn, create a 1/1 green Insect creature token. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new OncePerTurnTriggerEffect(new ConditionalEffect(new ControllerTurn(),
                        new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of()))));
    }
}
