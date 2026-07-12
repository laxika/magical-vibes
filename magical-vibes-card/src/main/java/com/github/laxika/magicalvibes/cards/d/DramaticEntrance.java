package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "111")
public class DramaticEntrance extends Card {

    public DramaticEntrance() {
        // You may put a green creature card from your hand onto the battlefield.
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardColorPredicate(CardColor.GREEN))),
                        "green creature"),
                "Put a green creature card from your hand onto the battlefield?"
        ));
    }
}
