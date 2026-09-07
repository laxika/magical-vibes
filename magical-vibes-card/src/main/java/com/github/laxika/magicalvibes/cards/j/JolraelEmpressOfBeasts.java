package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "115")
@CardRegistration(set = "TSB", collectorNumber = "81")
public class JolraelEmpressOfBeasts extends Card {

    public JolraelEmpressOfBeasts() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        new AnimatePermanentsEffect(
                                3, 3, List.of(), Set.of(), null, Set.of(),
                                GrantScope.TARGET_PLAYERS_LANDS, EffectDuration.UNTIL_END_OF_TURN)
                ),
                "{2}{G}, {T}, Discard two cards: All lands target player controls become 3/3 creatures until end of turn. They're still lands.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
