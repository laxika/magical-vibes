package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "52")
public class KioraBestsTheSeaGod extends Card {

    public KioraBestsTheSeaGod() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Kraken", 8, 8, CardColor.BLUE, List.of(CardSubtype.KRAKEN),
                Set.of(Keyword.HEXPROOF), Set.of()));

        PermanentPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addTrigger(Triggers.chapter(2),
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland));
        addTrigger(Triggers.chapter(2),
                new SkipNextUntapEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland));
        setSagaChapterTargetFilter(2, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent")));

        PermanentPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        addTrigger(Triggers.chapter(3),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT));
        addTrigger(Triggers.chapter(3), new UntapPermanentsEffect(TapUntapScope.TARGET));
        setSagaChapterTargetFilter(3, Set.of(
                new PermanentPredicateTargetFilter(
                        opponentPermanent, "Must target a permanent an opponent controls")));
    }
}
