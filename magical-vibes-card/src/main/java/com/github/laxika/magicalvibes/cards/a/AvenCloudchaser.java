package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.Set;

public class AvenCloudchaser extends Card {

    public AvenCloudchaser() {
        setNeedsTarget(true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
