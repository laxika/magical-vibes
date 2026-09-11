package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class ArchitectOfRestoration extends Card {

    public ArchitectOfRestoration() {
        addEffect(EffectSlot.ON_ATTACK, spiritToken());
        addEffect(EffectSlot.ON_BLOCK, spiritToken());
    }

    private static CreateTokenEffect spiritToken() {
        return new CreateTokenEffect("Spirit", 1, 1, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
    }
}
