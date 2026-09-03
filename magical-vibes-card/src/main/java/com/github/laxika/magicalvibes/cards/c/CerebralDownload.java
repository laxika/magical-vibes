package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "EOE", collectorNumber = "48")
public class CerebralDownload extends Card {

    public CerebralDownload() {
        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new SurveilEffect(artifactsYouControl));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
