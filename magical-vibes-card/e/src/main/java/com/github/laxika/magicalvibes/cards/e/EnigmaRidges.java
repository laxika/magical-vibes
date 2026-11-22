package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPermanentCountAmongOpponents;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMostLandsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "48")
public class EnigmaRidges extends Card {

    public EnigmaRidges() {
        PermanentPredicate land = new PermanentIsLandPredicate();
        addTrigger(Triggers.planeswalkToThis(), EachPlayerSearchesLibraryEffect.mustSearch(
                Subjects.except(Subjects.eachPlayer(),
                        Subjects.matching(Subjects.eachPlayer(), new PlayerControlsMostLandsPredicate())),
                CardPredicateUtils.basicLand(),
                new Sum(new GreatestPermanentCountAmongOpponents(land),
                        new Scaled(new PermanentCount(land, CountScope.CONTROLLER), -1)),
                LibrarySearchDestination.HAND));
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new DrawCardEffect(1),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                        "Put a land card from your hand onto the battlefield?")));
    }
}
