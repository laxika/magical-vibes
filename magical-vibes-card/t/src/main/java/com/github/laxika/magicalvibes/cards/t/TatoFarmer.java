package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "86")
@CardRegistration(set = "PIP", collectorNumber = "404")
@CardRegistration(set = "PIP", collectorNumber = "614")
@CardRegistration(set = "PIP", collectorNumber = "932")
public class TatoFarmer extends Card {

    public TatoFarmer() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new GiveControllerRadCountersEffect(2), "Get two rad counters?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.LAND))
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .targetPutIntoGraveyardFromLibraryThisTurn(true)
                        .enterTapped(true)
                        .build()),
                "Put target land card in a graveyard that was milled this turn onto the battlefield tapped under your control."));
    }
}
