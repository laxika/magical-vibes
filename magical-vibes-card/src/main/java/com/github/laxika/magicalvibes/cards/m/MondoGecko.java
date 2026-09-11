package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorAndGainHexproofFromItUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "46")
@CardRegistration(set = "TMT", collectorNumber = "231")
public class MondoGecko extends Card {

    public MondoGecko() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BecomeChosenColorAndGainHexproofFromItUntilEndOfTurnEffect()
                ),
                "{1}, Discard a card: Until end of turn, Mondo Gecko becomes the color of your choice "
                        + "and gains hexproof from that color."
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DrawCardEffect(new ColorsAmongControlledPermanents()));
    }
}
