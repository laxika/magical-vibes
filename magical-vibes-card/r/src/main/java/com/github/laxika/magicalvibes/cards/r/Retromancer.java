package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "USG", collectorNumber = "209")
public class Retromancer extends Card {

    public Retromancer() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggeringSpell()), 3));
    }
}
