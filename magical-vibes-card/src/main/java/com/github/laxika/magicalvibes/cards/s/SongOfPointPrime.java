package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "YEOE", collectorNumber = "12")
public class SongOfPointPrime extends Card {

    public SongOfPointPrime() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new ConjureCardInGraveyardEffect(CantorOfTheRefrain::new));
    }
}
