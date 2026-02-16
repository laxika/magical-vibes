package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;

public class Scalpelexis extends Card {

    public Scalpelexis() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardsRepeatOnDuplicateEffect(4));
    }
}
