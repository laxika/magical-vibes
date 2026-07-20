package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AKH", collectorNumber = "98")
public class LilianasMastery extends Card {

    public LilianasMastery() {
        // Zombies you control get +1/+1.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        // When this enchantment enters, create two 2/2 black Zombie creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.blackZombie(2));
    }
}
