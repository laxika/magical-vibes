package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "340")
public class ArvinoxTheMindFlail extends Card {

    public ArvinoxTheMindFlail() {
        // At the beginning of your end step, exile the bottom card of each opponent's library face down.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileBottomCardsToSourceEffect(new Fixed(1), true, true));

        // You may cast nonland permanent spells exiled with Arvinox, spending mana as though it
        // were mana of any color. Face-down cards exiled with a source are visible to its controller.
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true,
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                false, false, 0));
    }
}
