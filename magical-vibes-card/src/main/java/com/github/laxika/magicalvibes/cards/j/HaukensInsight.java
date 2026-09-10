package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;

public class HaukensInsight extends Card {

    public HaukensInsight() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardsToSourceEffect(1));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, true, 0, null, true, false, true));
    }
}
