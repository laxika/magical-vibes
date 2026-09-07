package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "236")
public class SpitfireHandler extends Card {

    public SpitfireHandler() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentIsSourceCardPredicate(),
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate()),
                "This creature can't block creatures with power greater than this creature's power"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: Spitfire Handler gets +1/+0 until end of turn."));
    }
}
