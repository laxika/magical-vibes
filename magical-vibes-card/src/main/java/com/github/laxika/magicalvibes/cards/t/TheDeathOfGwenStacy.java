package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "54")
@CardRegistration(set = "SPM", collectorNumber = "223")
public class TheDeathOfGwenStacy extends Card {

    public TheDeathOfGwenStacy() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DestroyTargetPermanentEffect(new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new EachPlayerMayDiscardOrLoseLifeEffect(3));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Must target a player"),
                        0, Integer.MAX_VALUE)));
    }
}
