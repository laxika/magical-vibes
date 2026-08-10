package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "246")
public class SoulFoundry extends Card {

    public SoulFoundry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileFromHandToImprintEffect(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                        "You may exile a creature card from your hand."));
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new CreateTokenCopyOfImprintedCardEffect(false, false)),
                "{X}, {T}: Create a token that's a copy of the exiled card. X is the mana value of that card."));
    }
}
