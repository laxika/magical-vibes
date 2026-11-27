package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "33a")
@CardRegistration(set = "ALL", collectorNumber = "33b")
public class SoldeviHeretic extends Card {

    public SoldeviHeretic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(PreventDamageEffect.nextToTargetCreature(2),
                        new TargetOpponentMayDrawCardEffect(Subjects.targetPlayer(1))),
                "{W}, {T}: Prevent the next 2 damage that would be dealt to target creature this turn. "
                        + "Target opponent may draw a card.",
                List.of(
                        TargetFilters.creature(),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                2,
                2
        ));
    }
}
