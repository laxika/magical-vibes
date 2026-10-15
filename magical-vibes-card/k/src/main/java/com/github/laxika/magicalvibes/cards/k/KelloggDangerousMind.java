package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "106")
@CardRegistration(set = "PIP", collectorNumber = "634")
@CardRegistration(set = "PIP", collectorNumber = "415")
@CardRegistration(set = "PIP", collectorNumber = "943")
public class KelloggDangerousMind extends Card {

    public KelloggDangerousMind() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(5,
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)
                ),
                "Sacrifice five Treasures: Gain control of target creature for as long as you control Kellogg. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
