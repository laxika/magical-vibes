package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "174")
@CardRegistration(set = "WOC", collectorNumber = "7")
@CardRegistration(set = "WOC", collectorNumber = "43")
public class SongbirdsBlessing extends Card {

    public SongbirdsBlessing() {
        // The attack trigger is the Aura's own ability, so its controller (whose library is revealed) is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature());
        addTrigger(Triggers.attacks(),
                new RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect(
                        new CardIsAuraPredicate()));
    }
}
