package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M20", collectorNumber = "167")
public class CavalierOfThorns extends Card {

    public CavalierOfThorns() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.putOneMatchingOntoBattlefieldRestToGraveyard(
                        5, new CardTypePredicate(CardType.LAND)));
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                SequenceEffect.of(
                        new ExileSourceCardFromGraveyardEffect(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                                .filter(new CardNotPredicate(new CardIsSelfPredicate()))
                                .targetGraveyard(true)
                                .build()),
                "Exile Cavalier of Thorns and put another target card from your graveyard on top of your library?"));
    }
}
