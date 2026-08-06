package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "252")
@CardRegistration(set = "INR", collectorNumber = "437")
public class WanderingMind extends Card {

    public WanderingMind() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
