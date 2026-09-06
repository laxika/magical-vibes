package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "35")
public class ThreeBlindMice extends Card {

    public ThreeBlindMice() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Mouse", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.MOUSE), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsTokenPredicate(), "Target must be a token you control")));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsTokenPredicate(), "Target must be a token you control")));

        addEffect(EffectSlot.SAGA_CHAPTER_IV, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
    }
}
