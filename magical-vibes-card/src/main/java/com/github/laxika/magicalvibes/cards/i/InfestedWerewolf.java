package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class InfestedWerewolf extends Card {

    private static final CreateTokenEffect CREATE_INSECTS = new CreateTokenEffect(
            2, "Insect", 1, 1, CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of());

    public InfestedWerewolf() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CREATE_INSECTS);
        addEffect(EffectSlot.ON_ATTACK, CREATE_INSECTS);
    }
}
