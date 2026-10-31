package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LibraryDecisionMaker;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "APC", collectorNumber = "70")
public class TahngarthsGlare extends Card {

    public TahngarthsGlare() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addSpellEffect(new ReorderTopCardsOfLibraryEffect(3, LibraryOwner.TARGET_PLAYER))
                .addSpellEffect(new ReorderTopCardsOfLibraryEffect(3, LibraryOwner.CONTROLLER,
                                LibraryDecisionMaker.TARGET_PLAYER));
    }
}
