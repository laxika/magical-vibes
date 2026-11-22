package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "109")
public class RiptideShapeshifter extends Card {

    public RiptideShapeshifter() {
        var creatureOfChosenType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE), new CardHasTypeChosenThisResolutionPredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(new SacrificeSelfCost(), new ChooseCreatureTypeAtResolutionEffect(),
                        new RevealCardsFromTopUntilEffect(creatureOfChosenType),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureOfChosenType)),
                        new ShuffleLibraryEffect(false)),
                "{2}{U}{U}, Sacrifice this creature: Choose a creature type. Reveal cards from the top of your library until you reveal a creature card of that type. Put that card onto the battlefield and shuffle the rest into your library."
        ));
    }
}
