package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "172")
public class PolukranosWorldEater extends Card {

    public PolukranosWorldEater() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(false, "{X}{X}{G}",
                List.of(new MonstrosityEffect(new XValue())),
                "{X}{X}{G}: Monstrosity X.")
                .withActivationCondition(new NotCondition(monstrous),
                        "This creature is already monstrous"));

        targetUpTo(new XValue(), TargetFilters.creatureAnOpponentControls(), 99)
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                        DealDividedDamageEffect.xAmongTargetCreaturesAtResolution())
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                        new EachTargetCreatureDealsPowerDamageToSourceEffect());
    }
}
