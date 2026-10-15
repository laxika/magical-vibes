package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "44")
public class RootwaterMystic extends Card {

    public RootwaterMystic() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.LOOK_ONLY)),
                "{1}{U}: Look at the top card of target player's library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
