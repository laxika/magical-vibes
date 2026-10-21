package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

public class FlamethrowerSonata extends Card {

    public FlamethrowerSonata() {
        addEffect(EffectSlot.SPELL, new DiscardCardThenEffect(
                null,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                        new LastDiscardedCardManaValue(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())),
                "a card",
                instantOrSorcery(),
                false,
                null,
                null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }

    private static CardAnyOfPredicate instantOrSorcery() {
        return new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
    }
}
