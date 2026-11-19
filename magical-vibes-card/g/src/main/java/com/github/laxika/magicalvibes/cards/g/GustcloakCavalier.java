package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSelfFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "22")
public class GustcloakCavalier extends Card {

    public GustcloakCavalier() {
        target(TargetFilters.creature()).addTrigger(Triggers.attacks(), new MayEffect(
                new TapPermanentsEffect(Subjects.target()),
                "Tap target creature?"
        ));

        addTrigger(Triggers.becomesBlocked(), new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(Subjects.source()),
                        new RemoveSelfFromCombatEffect()
                ),
                "Untap Gustcloak Cavalier and remove it from combat?"
        ));
    }
}
