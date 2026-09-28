package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "78")
@CardRegistration(set = "FIC", collectorNumber = "165")
public class BanonTheReturnersLeader extends Card {

    public BanonTheReturnersLeader() {
        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate()))));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new MayPayManaEffect(
                "{1}", new DiscardAndDrawCardEffect(), "Pay {1} and discard a card to draw a card?"));
    }
}
