package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "108")
public class ThrabenFoulbloods extends Card {

    public ThrabenFoulbloods() {
        // Delirium — This creature gets +1/+1 and has menace as long as there are four or more
        // card types among cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(1, 1, Set.of(Keyword.MENACE), GrantScope.SELF)));
    }
}
