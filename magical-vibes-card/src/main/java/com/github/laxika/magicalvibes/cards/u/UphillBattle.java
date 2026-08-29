package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "222")
public class UphillBattle extends Card {

    public UphillBattle() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.CREATURE), true));
    }
}
