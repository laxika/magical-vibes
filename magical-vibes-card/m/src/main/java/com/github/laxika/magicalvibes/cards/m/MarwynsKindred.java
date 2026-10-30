package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsToBattlefieldEffect;

@CardRegistration(set = "YDMU", collectorNumber = "16")
public class MarwynsKindred extends Card {

    public MarwynsKindred() {
        addEffect(EffectSlot.SPELL, new ConjureCardToBattlefieldEffect("DOM", "172"));
        addEffect(EffectSlot.SPELL, new ConjureCardsToBattlefieldEffect("LEA", "210", new XValue()));
    }
}
