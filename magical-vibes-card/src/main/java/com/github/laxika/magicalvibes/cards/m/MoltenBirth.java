package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "147")
public class MoltenBirth extends Card {

    public MoltenBirth() {
        // Create two 1/1 red Elemental creature tokens, ...
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Elemental", 1, 1, CardColor.RED,
                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()));
        // ... then flip a coin. If you win the flip, return Molten Birth to its owner's hand.
        addEffect(EffectSlot.SPELL, new FlipCoinWinEffect(ReturnToHandEffect.selfSpell()));
    }
}
