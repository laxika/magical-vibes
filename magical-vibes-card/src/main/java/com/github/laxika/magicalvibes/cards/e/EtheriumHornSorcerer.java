package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "PC2", collectorNumber = "91")
public class EtheriumHornSorcerer extends Card {

    public EtheriumHornSorcerer() {
        // {1}{U}{R}: Return this creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{R}", List.of(ReturnToHandEffect.self()),
                "{1}{U}{R}: Return this creature to its owner's hand."));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
