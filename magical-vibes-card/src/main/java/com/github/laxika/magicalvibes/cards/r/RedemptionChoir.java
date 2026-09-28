package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "42")
@CardRegistration(set = "LCC", collectorNumber = "74")
public class RedemptionChoir extends Card {

    public RedemptionChoir() {
        var returnPermanent = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardMaxManaValuePredicate(3)
                )))
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Coven(), returnPermanent));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new Coven(), returnPermanent));
    }
}
