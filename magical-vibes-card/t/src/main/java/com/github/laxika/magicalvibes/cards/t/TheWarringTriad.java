package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "99")
@CardRegistration(set = "FIC", collectorNumber = "193")
public class TheWarringTriad extends Card {

    public TheWarringTriad() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new GraveyardCardThreshold(8, null)),
                new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillControllerCost(1),
                        new AwardAnyColorManaEffect(new Fixed(1), true, false)
                ),
                "{T}, Mill a card: Target player adds one mana of any color. (Activate only as an instant.)",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
