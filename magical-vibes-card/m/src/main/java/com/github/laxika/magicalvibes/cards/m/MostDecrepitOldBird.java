package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpeakSecrets;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "HOB", collectorNumber = "49")
public class MostDecrepitOldBird extends Card {

    public MostDecrepitOldBird() {
        setBackFaceCard(new SpeakSecrets());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, new CardTruePredicate()),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpeakSecrets";
    }
}
