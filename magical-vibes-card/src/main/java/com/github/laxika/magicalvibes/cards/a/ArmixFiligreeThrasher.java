package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "103")
public class ArmixFiligreeThrasher extends Card {

    public ArmixFiligreeThrasher() {
        var defendingPlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()));
        var artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        var artifactCardsInYourGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.ARTIFACT), CountScope.CONTROLLER);
        var minusX = new Scaled(new Sum(artifactsYouControl, artifactCardsInYourGraveyard), -1);

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new BoostTargetCreatureEffect(minusX, minusX, defendingPlayerCreature),
                        "a card"),
                "Discard a card?"));
    }
}
