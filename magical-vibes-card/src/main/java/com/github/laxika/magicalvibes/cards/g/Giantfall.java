package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "141")
public class Giantfall extends Card {

    public Giantfall() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control deals damage equal to its power to target creature an opponent controls",
                        List.<CardEffect>of(new TargetCreatureDealsPowerDamageToAnyTargetEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()
                )
        )));
    }
}
