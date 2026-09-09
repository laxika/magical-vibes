package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "11")
public class OblivionSower extends Card {

    public OblivionSower() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_SELF_CAST,
                new ExileTopCardsOfTargetPlayerLibraryEffect(4));
        addEffect(EffectSlot.ON_SELF_CAST,
                new PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect());
    }
}
