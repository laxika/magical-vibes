package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "24")
public class TheQueenOfDale extends Card {

    public TheQueenOfDale() {
        CardNotPredicate noncreature = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
        CardNotPredicate nonland = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                SpellCastTriggerEffect.nth(1, noncreature, List.of(
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardCardThenEffect(
                                        null,
                                        CreateTokenEffect.whiteSoldier(1),
                                        "a card",
                                        nonland)))));
    }
}
