package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MKM", collectorNumber = "122")
@CardRegistration(set = "MKM", collectorNumber = "306")
public class DemandAnswers extends Card {

    public DemandAnswers() {
        addEffect(EffectSlot.SPELL,
                new SacrificePermanentOrDiscardCardCost(new PermanentIsArtifactPredicate(), "an artifact"));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
