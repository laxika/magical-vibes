package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "274")
public class VhatiIlDal extends Card {

    public VhatiIlDal() {
        // {T}: Until end of turn, target creature has base power 1 or base toughness 1.
        // Both modes apply to the same target, so the target is chosen on activation and the
        // mode is picked as the ability resolves. Each mode sets only its own layer-7b
        // component, leaving the creature's other printed base value alone.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("It has base power 1",
                                SetBasePowerToughnessEffect.powerOnly(1)),
                        new ChooseOneEffect.ChooseOneOption("It has base toughness 1",
                                SetBasePowerToughnessEffect.toughnessOnly(1))))),
                "{T}: Until end of turn, target creature has base power 1 or base toughness 1."));
    }
}
