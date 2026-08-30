package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "115")
public class VraskaBetrayalsSting extends Card {

    public VraskaBetrayalsSting() {
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(), new LoseLifeEffect(1), new ProliferateEffect()),
                "0: You draw a card and lose 1 life. Proliferate."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.TARGET),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.TREASURE),
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                        "{T}, Sacrifice this artifact: Add one mana of any color."
                                ),
                                GrantScope.TARGET,
                                null,
                                EffectDuration.PERMANENT
                        )
                ),
                "-2: Target creature becomes a Treasure artifact with \"{T}, Sacrifice this artifact: Add one mana of any color.\" and loses all other card types and abilities.",
                TargetFilters.creature()
        ));

        DynamicAmount poisonAmount = new Max(
                new Fixed(0),
                new Sum(new Fixed(9), new Scaled(new TargetPlayerPoisonCounters(), -1))
        );
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new GiveTargetPlayerPoisonCountersEffect(poisonAmount)),
                "-9: If target player has fewer than nine poison counters, they get a number of poison counters equal to the difference.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
