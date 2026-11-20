package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "35")
public class GustcloakRunner extends Card {

    public GustcloakRunner() {
        addTrigger(Triggers.becomesBlocked(), new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(Subjects.source()),
                        new RemoveTargetFromCombatEffect(Subjects.source())
                ),
                "Untap Gustcloak Runner and remove it from combat?"
        ));
    }
}
