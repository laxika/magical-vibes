package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "17")
public class RazorHippogriff extends Card {

    public RazorHippogriff() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnCardFromGraveyardEffect(
                        GraveyardChoiceDestination.HAND,
                        new CardTypePredicate(CardType.ARTIFACT),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false, false, false, null, true
                )
        );
    }
}
