package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeshirosLivingLegacy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "210")
public class TalesOfMasterSeshiro extends Card {

    public TalesOfMasterSeshiro() {
        setBackFaceCard(new SeshirosLivingLegacy());

        PermanentPredicate creatureOrVehicleYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)
                ))
        ));

        addChapterEffects(EffectSlot.SAGA_CHAPTER_I, creatureOrVehicleYouControl);
        addChapterEffects(EffectSlot.SAGA_CHAPTER_II, creatureOrVehicleYouControl);
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    private void addChapterEffects(EffectSlot chapter, PermanentPredicate targetRestriction) {
        addEffect(chapter, PutCounterOnTargetPermanentEffect.withTargetRestriction(
                CounterType.PLUS_ONE_PLUS_ONE, 1, targetRestriction));
        addEffect(chapter, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET, targetRestriction));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeshirosLivingLegacy";
    }
}
