package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOI", collectorNumber = "192")
public class WolfOfDevilsBreach extends Card {

    public WolfOfDevilsBreach() {
        // Whenever this creature attacks, you may pay {1}{R} and discard a card. If you do, this
        // creature deals damage to target creature or planeswalker equal to the discarded card's
        // mana value.
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay(
                AllOfCost.of(new PayManaCost("{1}{R}"), new DiscardCardTypeCost(null, null)),
                DealDamageEffect.toTargetCreatureOrPlaneswalker(new LastDiscardedCardManaValue()))
                .withPrompt("Pay {1}{R} and discard a card to deal damage to target creature or planeswalker?"));
    }
}
