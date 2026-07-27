package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;


@CardRegistration(set = "CON", collectorNumber = "143")
public class ReliquaryTower extends Card {

    public ReliquaryTower() {
        // You have no maximum hand size.
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
