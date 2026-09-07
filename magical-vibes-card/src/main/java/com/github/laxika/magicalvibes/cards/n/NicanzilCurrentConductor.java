package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "236")
@CardRegistration(set = "LCI", collectorNumber = "306")
public class NicanzilCurrentConductor extends Card {

    public NicanzilCurrentConductor() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLORES, new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                        "Put a land card from your hand onto the battlefield tapped?")));
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLORES, new TriggeringCardConditionalEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
