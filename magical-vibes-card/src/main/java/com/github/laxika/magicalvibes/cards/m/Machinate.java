package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "24")
public class Machinate extends Card {

    public Machinate() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(artifactsYouControl));
    }
}
