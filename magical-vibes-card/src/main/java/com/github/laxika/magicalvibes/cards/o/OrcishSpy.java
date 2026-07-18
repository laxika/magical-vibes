package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "208")
public class OrcishSpy extends Card {

    public OrcishSpy() {
        // {T}: Look at the top three cards of target player's library. Private look — the cards stay
        // on top in their original order (no rearranging).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LookAtTopCardsOfTargetLibraryEffect(3, TargetLibraryAction.LOOK_ONLY)),
                "{T}: Look at the top three cards of target player's library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
