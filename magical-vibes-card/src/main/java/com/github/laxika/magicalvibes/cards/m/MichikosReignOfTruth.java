package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PortraitOfMichiko;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "29")
public class MichikosReignOfTruth extends Card {

    public MichikosReignOfTruth() {
        setBackFaceCard(new PortraitOfMichiko());

        PermanentCount artifactsAndEnchantmentsYouControl = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                CountScope.CONTROLLER);

        target(TargetFilters.creature()).addEffect(EffectSlot.SAGA_CHAPTER_I,
                new BoostTargetCreatureEffect(
                        artifactsAndEnchantmentsYouControl, artifactsAndEnchantmentsYouControl));
        target(TargetFilters.creature()).addEffect(EffectSlot.SAGA_CHAPTER_II,
                new BoostTargetCreatureEffect(
                        artifactsAndEnchantmentsYouControl, artifactsAndEnchantmentsYouControl));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "PortraitOfMichiko";
    }
}
