package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "164")
public class RakdosRiteknife extends Card {

    public RakdosRiteknife() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new CountersOnSource(CounterType.BLOOD),
                new Fixed(0),
                GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new PutCountersOnSelfEffect(CounterType.BLOOD)),
                "{T}, Sacrifice a creature: Put a blood counter on Rakdos Riteknife."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificePermanentsEffect(
                                new CountersOnSource(CounterType.BLOOD),
                                new PermanentTruePredicate(),
                                SacrificeRecipient.TARGET_PLAYER)
                ),
                "{B}{R}, Sacrifice this Equipment: Target player sacrifices a permanent of their choice for each blood counter on Rakdos Riteknife.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
