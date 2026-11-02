package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "51")
public class FieryEncore extends Card {

    public FieryEncore() {
        addSpellEffect(new DiscardCardThenEffect(
                null,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                        new LastDiscardedCardManaValue()),
                "a card",
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                false,
                null,
                null));
        addSpellEffect(new DrawCardEffect());
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
