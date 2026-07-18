package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "4ED", collectorNumber = "299")
public class BlackVise extends Card {

    public BlackVise() {
        // "At the beginning of the chosen player's upkeep, Black Vise deals X damage to that player,
        // where X is the number of cards in their hand minus 4." In this 1v1 engine the chosen
        // opponent is the sole opponent, so the trigger fires on that opponent's upkeep. X clamps to
        // 0 damage when the hand has 4 or fewer cards (the damage handler ignores non-positive amounts).
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new Sum(new CardsInHand(CountScope.TARGET_PLAYER), new Fixed(-4)),
                        DamageRecipient.TARGET_PLAYER));
    }
}
