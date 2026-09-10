package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "192")
public class KhalniAmbush extends Card {

    public KhalniAmbush() {
        setBackFaceCard(new KhalniTerritory());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature you don't control",
                        List.of(new FightTargetsEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())),
                new ChooseOneEffect.ChooseOneOption("Khalni Territory", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "KhalniTerritory";
    }
}
