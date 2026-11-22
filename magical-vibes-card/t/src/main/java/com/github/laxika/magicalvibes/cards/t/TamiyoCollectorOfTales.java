package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "220")
@CardRegistration(set = "SLD", collectorNumber = "525")
public class TamiyoCollectorOfTales extends Card {

    public TamiyoCollectorOfTales() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addStaticEffect(new OpponentEffectsCantCauseDiscardEffect());
        addStaticEffect(new OpponentEffectsCantCauseSacrificeEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND),
                        RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(4)),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(named))),
                "+1: Choose a nonland card name, then reveal the top four cards of your library. Put all cards with the chosen name from among them into your hand and the rest into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build()),
                "−3: Return target card from your graveyard to your hand."
        ));
    }
}
