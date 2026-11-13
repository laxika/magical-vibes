package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDrawsCardsEqualToAttachedCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerOtherThanTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "46")
@CardRegistration(set = "DBL", collectorNumber = "46")
public class CurseOfSurveillance extends Card {

    public CurseOfSurveillance() {
        // Enchant player.
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        target(new PlayerPredicateTargetFilter(new PlayerOtherThanTriggeringPlayerPredicate(),
                "Target must be a player other than the enchanted player"), 0, Integer.MAX_VALUE)
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                        new EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
    }
}
