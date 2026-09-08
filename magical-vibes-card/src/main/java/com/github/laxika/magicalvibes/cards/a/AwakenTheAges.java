package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/** Awaken the Ages, the prepare spell of Strife Scholar // Awaken the Ages (SOS 131). */
public class AwakenTheAges extends Card {

    public AwakenTheAges() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Spirit", 2, 2,
                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SPIRIT)));
    }
}
