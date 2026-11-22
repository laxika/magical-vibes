package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "249")
public class BloodlineShaman extends Card {

    public BloodlineShaman() {
        var creatureOfChosenType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE), new CardHasTypeChosenThisResolutionPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCreatureTypeAtResolutionEffect(),
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(creatureOfChosenType)),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(new CardNotPredicate(creatureOfChosenType)))),
                "{T}: Choose a creature type. Reveal the top card of your library. If that card is a creature card of the chosen type, put it into your hand. Otherwise, put it into your graveyard."
        ));
    }
}
