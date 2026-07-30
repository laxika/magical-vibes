package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M13", collectorNumber = "57")
public class JacesPhantasm extends Card {

    public JacesPhantasm() {
        // This creature gets +4/+4 as long as an opponent has ten or more cards in their graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new OpponentGraveyardAtLeast(10),
                new StaticBoostEffect(4, 4, GrantScope.SELF)));
    }
}
