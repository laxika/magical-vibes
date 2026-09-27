package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOC", collectorNumber = "174")
public class SongbirdsBlessing extends Card {

    public SongbirdsBlessing() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ATTACK,
                new RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect(
                        new CardIsAuraPredicate()));
    }
}
