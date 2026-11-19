package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "133a")
@CardRegistration(set = "ALL", collectorNumber = "133b")
public class SoldeviSteamBeast extends Card {

    public SoldeviSteamBeast() {
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.becomesTapped(), new GainLifeEffect(2, Subjects.targetPlayer()));

        // {2}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new RegenerateEffect()),
                "{2}: Regenerate Soldevi Steam Beast."
        ));
    }
}
