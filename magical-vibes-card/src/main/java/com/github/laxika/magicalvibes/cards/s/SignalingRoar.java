package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

import java.util.List;
import java.util.Set;

public class SignalingRoar extends Card {

    public SignalingRoar() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Soldier", 2, 2, CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
