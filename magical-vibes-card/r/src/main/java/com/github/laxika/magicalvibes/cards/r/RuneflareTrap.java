package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.OpponentDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "146")
public class RuneflareTrap extends Card {

    public RuneflareTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{R}")),
                new OpponentDrewAtLeastCardsThisTurn(3),
                false
        ));
        addEffect(EffectSlot.SPELL,
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                        DamageRecipient.TARGET_PLAYER));
    }
}
