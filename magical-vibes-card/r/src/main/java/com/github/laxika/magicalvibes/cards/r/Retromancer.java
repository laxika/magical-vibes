package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "209")
public class Retromancer extends Card {

    public Retromancer() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new DealDamageToTriggeringSpellControllerEffect(3));
    }
}
