package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "172")
@CardRegistration(set = "DDL", collectorNumber = "43")
@CardRegistration(set = "PIO", collectorNumber = "190")
public class PolukranosWorldEater extends Card {

    public PolukranosWorldEater() {
        addActivatedAbility(new ActivatedAbility(false, "{X}{X}{G}",
                List.of(new MonstrosityEffect(new XValue())),
                "{X}{X}{G}: Monstrosity X."));

        targetUpTo(new XValue(), TargetFilters.creatureAnOpponentControls(), 99)
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                        DealDividedDamageEffect.xAmongTargetCreaturesAtResolution())
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                        new EachTargetCreatureDealsPowerDamageToSourceEffect());
    }
}
