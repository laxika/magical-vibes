package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ODY", collectorNumber = "241")
public class GorillaTitan extends Card {

    public GorillaTitan() {
        // This creature gets +4/+4 as long as there are no cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new GraveyardCardThreshold(1, null)),
                new StaticBoostEffect(4, 4, GrantScope.SELF)));
    }
}
