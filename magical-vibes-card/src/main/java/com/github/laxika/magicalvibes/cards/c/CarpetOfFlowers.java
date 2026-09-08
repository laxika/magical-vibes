package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceAddedManaThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "USG", collectorNumber = "240")
public class CarpetOfFlowers extends Card {

    public CarpetOfFlowers() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        SpellTarget opponentTarget = target(opponent);
        opponentTarget.addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, ability());
        opponentTarget.addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, ability());
    }

    private CardEffect ability() {
        PermanentCount islands = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.TARGET_PLAYER);
        return new ConditionalEffect(
                new NotCondition(new SourceAddedManaThisTurn()),
                new MayEffect(
                        new AwardAnyColorManaEffect(islands, ManaSpendRestriction.NONE, null,
                                false, true, false, true, false, false),
                        "Add mana with Carpet of Flowers?"));
    }
}
