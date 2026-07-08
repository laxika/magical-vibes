package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "30")
public class MilitiasPride extends Card {

    public MilitiasPride() {
        // Whenever a nontoken creature you control attacks, you may pay {W}. If you do,
        // create a 1/1 white Kithkin Soldier creature token that's tapped and attacking.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()),
                        new MayPayManaEffect("{W}",
                                new CreateTokenEffect(1, "Kithkin Soldier", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER), true),
                                "Pay {W} to create a 1/1 white Kithkin Soldier token that's tapped and attacking?")));
    }
}
