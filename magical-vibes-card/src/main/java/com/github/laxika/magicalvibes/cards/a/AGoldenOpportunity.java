package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "YWOE", collectorNumber = "13")
public class AGoldenOpportunity extends Card {

    public AGoldenOpportunity() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ConjureCardToBattlefieldEffect("Gilded Goose"));
        addEffect(EffectSlot.SAGA_CHAPTER_II, chapterTwoOrThreeEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, chapterTwoOrThreeEffect());
    }

    private static MayPayTapAndSacrificePermanentEffect chapterTwoOrThreeEffect() {
        return new MayPayTapAndSacrificePermanentEffect(
                new TapMultiplePermanentsCost(
                        1, new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                new PermanentIsArtifactPredicate(),
                new ConjureCardToBattlefieldEffect("Golden Egg"),
                "an artifact",
                "Tap an untapped Bird you control and sacrifice an artifact?");
    }
}
