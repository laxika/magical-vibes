package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "138")
@CardRegistration(set = "2X2", collectorNumber = "185")
public class BloodwaterEntity extends Card {

    public BloodwaterEntity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )))
                        .targetGraveyard(true)
                        .build(),
                "Put target instant or sorcery card from your graveyard on top of your library?"));
    }
}
