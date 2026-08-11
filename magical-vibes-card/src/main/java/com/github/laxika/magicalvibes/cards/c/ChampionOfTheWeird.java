package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBlightsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "95")
public class ChampionOfTheWeird extends Card {

    public ChampionOfTheWeird() {
        addEffect(EffectSlot.SPELL, new BeholdAndExileCost(CardSubtype.GOBLIN));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(1), new TargetPlayerBlightsEffect(2)),
                "Pay 1 life, Blight 2: Target opponent blights 2. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
