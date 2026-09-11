package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

public class AgadeemTheUndercrypt extends Card {

    public AgadeemTheUndercrypt() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(3));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
