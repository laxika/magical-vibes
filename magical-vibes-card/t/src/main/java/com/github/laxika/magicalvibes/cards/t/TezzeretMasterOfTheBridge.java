package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1701")
@CardRegistration(set = "WAR", collectorNumber = "275")
public class TezzeretMasterOfTheBridge extends Card {

    public TezzeretMasterOfTheBridge() {
        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);

        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.PLANESWALKER))),
                artifactsYouControl, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        DealDamageEffect.to(Subjects.eachOpponent(), artifactsYouControl),
                        new GainLifeEffect(artifactsYouControl)),
                "+2: Tezzeret deals X damage to each opponent, where X is the number of artifacts you control. "
                        + "You gain X life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                        new CardTypePredicate(CardType.ARTIFACT))),
                "−3: Return target artifact card from your graveyard to your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(ExileTopCardOfOwnLibraryEffect.of(Subjects.you(), 10),
                        ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(Subjects.cardsThisWay(StepKind.EXILE,
                                new CardTypePredicate(CardType.ARTIFACT)))),
                "−8: Exile the top ten cards of your library. Put all artifact cards from among them onto the battlefield."
        ));
    }
}
