package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "155")
public class SallySparrow extends Card {

    public SallySparrow() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new OncePerTurnTriggerEffect(CreateTokenEffect.ofClueToken(1)));
    }
}
