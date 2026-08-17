package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "199")
public class CrawlingSensation extends Card {

    public CrawlingSensation() {
        // At the beginning of your upkeep, you may mill two cards.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));

        // Whenever one or more land cards are put into your graveyard from anywhere for the first
        // time each turn, create a 1/1 green Insect creature token.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new OncePerTurnTriggerEffect(new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of())));
    }
}
