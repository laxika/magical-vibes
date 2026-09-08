package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HowlpackAvenger;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "162")
public class IllTemperedLoner extends Card {

    public IllTemperedLoner() {
        setBackFaceCard(new HowlpackAvenger());

        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DealDamageToAnyTargetEffect(new XValue()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostSelfEffect(2, 0)),
                "{1}{R}: This creature gets +2/+0 until end of turn."));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HowlpackAvenger";
    }
}
