package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "72")
@CardRegistration(set = "LTC", collectorNumber = "152")
public class TooGreedilyTooDeep extends Card {

    public TooGreedilyTooDeep() {
        // Put target creature card from a graveyard onto the battlefield under your control.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .targetGraveyard(true)
                .build());

        // That creature deals damage equal to its power to each other creature.
        addEffect(EffectSlot.SPELL, new ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffect());
    }
}
