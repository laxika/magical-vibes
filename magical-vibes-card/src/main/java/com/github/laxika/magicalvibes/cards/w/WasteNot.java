package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Three separate opponent-discard triggers, each gated on the discarded card's type by a
 * {@link TriggeringCardConditionalEffect}. A discarded card matches exactly one of the three, so
 * exactly one ability triggers per discarded card.
 */
@CardRegistration(set = "M15", collectorNumber = "122")
public class WasteNot extends Card {

    public WasteNot() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.CREATURE),
                CreateTokenEffect.blackZombie(1)));
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                new AwardManaEffect(ManaColor.BLACK, 2)));
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                new DrawCardEffect()));
    }
}
