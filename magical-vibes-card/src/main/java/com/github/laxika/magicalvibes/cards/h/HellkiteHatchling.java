package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DevouredCreature;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "CON", collectorNumber = "111")
public class HellkiteHatchling extends Card {

    public HellkiteHatchling() {
        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // This creature has flying and trample if it devoured a creature.
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new DevouredCreature(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new DevouredCreature(), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
