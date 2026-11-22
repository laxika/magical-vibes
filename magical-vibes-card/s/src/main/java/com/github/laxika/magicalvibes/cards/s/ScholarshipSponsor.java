package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPermanentCountAmongOpponents;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMostLandsPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "22")
public class ScholarshipSponsor extends Card {

    public ScholarshipSponsor() {
        PermanentPredicate land = new PermanentIsLandPredicate();
        addTrigger(Triggers.enters(ObjectRelation.THIS), EachPlayerSearchesLibraryEffect.mustSearch(
                Subjects.except(Subjects.eachPlayer(),
                        Subjects.matching(Subjects.eachPlayer(), new PlayerControlsMostLandsPredicate())),
                CardPredicateUtils.basicLand(),
                new Sum(new GreatestPermanentCountAmongOpponents(land),
                        new Scaled(new PermanentCount(land, CountScope.CONTROLLER), -1)),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
