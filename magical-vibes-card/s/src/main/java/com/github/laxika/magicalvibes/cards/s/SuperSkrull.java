package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "115")
public class SuperSkrull extends Card {

    public SuperSkrull() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new CreateTokenEffect("Wall", 0, 4, null,
                        List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of())),
                "{2}{W}: Create a 0/4 colorless Wall creature token with defender."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostSelfEffect(4, 4)),
                "{3}{G}: Super-Skrull gets +4/+4 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "{4}{R}: Super-Skrull deals 4 damage to target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}",
                List.of(new DrawCardForTargetPlayerEffect(4, false, true)),
                "{5}{U}: Target player draws four cards.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
