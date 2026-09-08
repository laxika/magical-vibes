package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FUT", collectorNumber = "86")
public class GraveScrabbler extends Card {

    public GraveScrabbler() {
        addCastingOption(new MadnessCast("{1}{B}"));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForMadnessCost(),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build(),
                        "Return target creature card from a graveyard to its owner's hand?")));
    }
}
