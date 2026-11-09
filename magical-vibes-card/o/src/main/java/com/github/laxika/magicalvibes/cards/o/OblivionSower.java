package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "11")
@CardRegistration(set = "DDP", collectorNumber = "41")
@CardRegistration(set = "M3C", collectorNumber = "158")
@CardRegistration(set = "OTC", collectorNumber = "77")
public class OblivionSower extends Card {

    public OblivionSower() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youCastThisSpell(),
                new ExileTopCardsOfTargetPlayerLibraryEffect(4),
                new PutAnyNumberOfOwnedLandCardsFromExileOntoBattlefieldEffect());
    }
}
