package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "6")
@CardRegistration(set = "ATQ", collectorNumber = "99")
public class MartyrsOfKorlis extends Card {

    public MartyrsOfKorlis() {
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToSelfEffect(
                new PermanentIsArtifactPredicate(), true));
    }
}
