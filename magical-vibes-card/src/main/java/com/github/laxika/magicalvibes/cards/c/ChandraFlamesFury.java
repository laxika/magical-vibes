package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "294")
public class ChandraFlamesFury extends Card {

    public ChandraFlamesFury() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToAnyTargetEffect(2)),
                "+1: Chandra, Flame's Fury deals 2 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new DealDamageToTargetCreatureEffect(4),
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER)
                ),
                "−2: Chandra, Flame's Fury deals 4 damage to target creature and 2 damage to that creature's controller."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new DealDamageToTargetAndTheirCreaturesEffect(10)),
                "−8: Chandra, Flame's Fury deals 10 damage to target player and each creature that player controls.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
