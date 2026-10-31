package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MSC", collectorNumber = "570")
public class PatriotYoungAvenger extends Card {

    public PatriotYoungAvenger() {
        addStaticEffect(new ConditionalEffect(new Equipped(),
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES)));
    }
}
