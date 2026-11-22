package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "275")
@CardRegistration(set = "TPR", collectorNumber = "216")
public class WoodSage extends Card {

    public WoodSage() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCardNameAtResolutionEffect(CardType.CREATURE),
                        RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(4)),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(named))),
                "{T}: Choose a creature card name. Reveal the top four cards of your library and put "
                        + "all of them with that name into your hand. Put the rest into your graveyard."));
    }
}
