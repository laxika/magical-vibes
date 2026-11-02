package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerExilesRandomHandCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class RonaTolarianObliterator extends Card {

    public RonaTolarianObliterator() {
        addTrigger(Triggers.dealtDamage(), new DamageSourceControllerExilesRandomHandCardEffect());
    }
}
