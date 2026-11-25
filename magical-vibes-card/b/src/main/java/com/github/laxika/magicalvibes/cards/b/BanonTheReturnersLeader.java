package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "78")
@CardRegistration(set = "FIC", collectorNumber = "165")
public class BanonTheReturnersLeader extends Card {

    public BanonTheReturnersLeader() {
        addStaticEffect(new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate()))));
        addTrigger(Triggers.youAttack(), UnlessPaysEffect.youMayPay(
                AllOfCost.of(new PayManaCost("{1}"), new DiscardCardTypeCost(null, null)), new DrawCardEffect(1))
                .withPrompt("Pay {1} and discard a card to draw a card?"));
    }
}
