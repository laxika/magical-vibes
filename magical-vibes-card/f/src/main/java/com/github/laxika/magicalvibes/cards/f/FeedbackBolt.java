package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "5DN", collectorNumber = "64")
public class FeedbackBolt extends Card {

    public FeedbackBolt() {
        addSpellEffect(DealDamageEffect.toTargetPlayerOrPlaneswalker(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
    }
}
