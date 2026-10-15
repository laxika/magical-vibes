package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "4")
public class AgentOfRaffine extends Card {

    public AgentOfRaffine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffect()),
                "{2}, {T}: Choose target opponent. Conjure a duplicate of the top card of their library into your hand. It perpetually gains \"You may spend mana as though it were mana of any color to cast this spell.\" Then they exile the top card of their library face down.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
