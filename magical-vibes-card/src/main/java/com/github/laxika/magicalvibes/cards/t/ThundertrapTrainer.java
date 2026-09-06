package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "78")
public class ThundertrapTrainer extends Card {

    public ThundertrapTrainer() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4,
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
