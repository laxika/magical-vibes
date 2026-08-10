package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "159")
public class CrystalShard extends Card {

    public CrystalShard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ReturnTargetCreatureUnlessControllerPaysEffect("{1}")),
                "{3}, {T}: Return target creature to its owner's hand unless its controller pays {1}.",
                TargetFilters.creature()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new ReturnTargetCreatureUnlessControllerPaysEffect("{1}")),
                "{U}, {T}: Return target creature to its owner's hand unless its controller pays {1}.",
                TargetFilters.creature()));
    }
}
