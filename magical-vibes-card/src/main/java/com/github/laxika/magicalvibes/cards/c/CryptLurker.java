package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "93")
public class CryptLurker extends Card {

    public CryptLurker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice a creature",
                                new SacrificePermanentThenEffect(
                                        new PermanentIsCreaturePredicate(),
                                        new DrawCardEffect(1),
                                        "a creature")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a creature card",
                                new DiscardCardThenEffect(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new DrawCardEffect(1),
                                        "a creature card"))
                )),
                "Sacrifice a creature or discard a creature card to draw a card?"
        ));
    }
}
