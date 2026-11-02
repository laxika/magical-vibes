package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "54")
@CardRegistration(set = "SPM", collectorNumber = "223")
@CardRegistration(set = "OM1", collectorNumber = "58")
public class TheDeathOfGwenStacy extends Card {

    public TheDeathOfGwenStacy() {
        addTrigger(Triggers.chapter(1),
                new DestroyTargetPermanentEffect(new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.chapter(2),
                new EachPlayerMayDiscardOrLoseLifeEffect(3));

        addTrigger(Triggers.chapter(3),
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Must target a player"),
                        0, Integer.MAX_VALUE)));
    }
}
