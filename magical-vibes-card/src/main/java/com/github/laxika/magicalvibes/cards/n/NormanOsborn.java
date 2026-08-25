package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "39")
@CardRegistration(set = "SPM", collectorNumber = "220")
public class NormanOsborn extends Card {

    public NormanOsborn() {
        setBackFaceCard(new GreenGoblin());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawDiscardAndConniveEffect());

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
