package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RNA", collectorNumber = "155")
public class AzoriusSkyguard extends Card {

    public AzoriusSkyguard() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES));
    }
}
