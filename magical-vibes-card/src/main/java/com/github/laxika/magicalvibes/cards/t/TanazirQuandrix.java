package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "240")
public class TanazirQuandrix extends Card {

    public TanazirQuandrix() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DoublePlusOneCountersOnTargetCreatureEffect());

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SetAllOwnCreaturesBasePowerToughnessEffect(
                        new SourcePower(),
                        new SourceToughness(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())),
                "Have other creatures you control become equal to Tanazir Quandrix's power and toughness?"
        ));
    }
}
