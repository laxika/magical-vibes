package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNameInControllerGraveyardPredicate;
import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "250")
public class CandlesOfLeng extends Card {

    public CandlesOfLeng() {
        var sameNameInGraveyard = new CardNameInControllerGraveyardPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new ConditionalEffect(new NotCondition(HappenedThisWay.toACard(StepKind.REVEAL, sameNameInGraveyard)),
                                new DrawCardEffect()),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(sameNameInGraveyard))),
                "{4}, {T}: Reveal the top card of your library. If it has the same name as a card in your graveyard, "
                        + "put it into your graveyard. Otherwise, draw a card."
        ));
    }
}
