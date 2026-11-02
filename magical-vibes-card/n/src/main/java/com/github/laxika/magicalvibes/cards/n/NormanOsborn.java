package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "39")
@CardRegistration(set = "SPM", collectorNumber = "220")
@CardRegistration(set = "OM1", collectorNumber = "30")
public class NormanOsborn extends Card {

    public NormanOsborn() {
        setBackFaceCard(new GreenGoblin());
        setModalDoubleFaced(true);

        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawDiscardAndConniveEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{B}{R}",
                List.of(new TransformSelfEffect()),
                "{1}{U}{B}{R}: Transform Norman Osborn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "GreenGoblin";
    }
}
