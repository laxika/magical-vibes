package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "52")
public class KioraBestsTheSeaGod extends Card {

    public KioraBestsTheSeaGod() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                "Kraken", 8, 8, CardColor.BLUE, List.of(CardSubtype.KRAKEN),
                Set.of(Keyword.HEXPROOF), Set.of()));

        PermanentPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SkipNextUntapEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent")));

        PermanentPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new UntapPermanentsEffect(TapUntapScope.TARGET));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new PermanentPredicateTargetFilter(
                        opponentPermanent, "Must target a permanent an opponent controls")));
    }
}
