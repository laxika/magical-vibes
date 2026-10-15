package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "BOT", collectorNumber = "9")
@CardRegistration(set = "BOT", collectorNumber = "23")
public class CyclonusTheSaboteur extends Card {

    public CyclonusTheSaboteur() {
        setBackFaceCard(new CyclonusCybertronianFighter());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{5}{U}{B}"));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DrawDiscardAndConniveEffect(),
                new ConditionalEffect(new SourcePowerAtLeast(5), new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "CyclonusCybertronianFighter";
    }
}
