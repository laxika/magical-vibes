package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MycoidMaze;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ScryBeforeExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "217")
public class TwistsAndTurns extends Card {

    public TwistsAndTurns() {
        setBackFaceCard(new MycoidMaze());

        addEffect(EffectSlot.STATIC, new ScryBeforeExploreReplacementEffect());
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect(true));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ConditionalEffect(new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "MycoidMaze";
    }
}
