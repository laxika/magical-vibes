package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "MB1", collectorNumber = "224")
public class ThoughtVessel extends Card {

    public ThoughtVessel() {
        // You have no maximum hand size.
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
