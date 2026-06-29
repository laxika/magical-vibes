package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;

@CardRegistration(set = "DKA", collectorNumber = "79")
public class Wakedancer extends Card {

    public Wakedancer() {
        // Morbid — When Wakedancer enters the battlefield, if a creature died this turn,
        // create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MorbidConditionalEffect(
                CreateTokenEffect.blackZombie(1)
        ));
    }
}
