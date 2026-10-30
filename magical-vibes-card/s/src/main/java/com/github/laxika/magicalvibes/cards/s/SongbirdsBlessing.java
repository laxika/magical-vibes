package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOC", collectorNumber = "174")
@CardRegistration(set = "WOC", collectorNumber = "7")
@CardRegistration(set = "WOC", collectorNumber = "43")
public class SongbirdsBlessing extends Card {

    public SongbirdsBlessing() {
        // The attack trigger is the Aura's own ability, so its controller (whose library is revealed) is the
        // Aura's controller even when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ATTACK,
                new RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect(
                        new CardIsAuraPredicate()));
    }
}
