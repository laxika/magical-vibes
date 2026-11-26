package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "18")
public class SpelltitheEnforcer extends Card {

    public SpelltitheEnforcer() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(), "{1}",
                        new SacrificePermanentsEffect(1, new PermanentTruePredicate(), Subjects.triggeringPlayer())));
    }
}
