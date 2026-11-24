package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "115")
public class SpyNetwork extends Card {

    public SpyNetwork() {
        // Look at target player's hand, the top card of that player's library, and any face-down creatures they
        // control.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new LookAtHandEffect(Subjects.targetPlayer()))
                .addSpellEffect(LookAtTopCardsOfTargetLibraryEffect.lookingAt(1,
                        Subjects.targetPlayerDeclaredElsewhere()))
                .addSpellEffect(new LookAtFaceDownCreatureEffect(Subjects.controlledBy(
                        Subjects.targetPlayerDeclaredElsewhere(), new PermanentAllOfPredicate(List.of(
                                new PermanentIsFaceDownPredicate(), new PermanentIsCreaturePredicate())))));
        // Look at the top four cards of your library, then put them back in any order.
        addSpellEffect(new ReorderTopCardsOfLibraryEffect(4, Subjects.you()));
    }
}
