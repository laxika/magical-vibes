package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "10")
public class DevotedCropMate extends Card {

    public DevotedCropMate() {
        // Exert as it attacks. When you do, return target creature card with mana value 2 or less
        // from your graveyard to the battlefield. (Modeled as an optional attack trigger, matching
        // Bishop of Rebirth's exert reanimation.)
        MayEffect returnEffect = new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(2)
                        )))
                        .build(),
                "You may return target creature card with mana value 2 or less from your graveyard to the battlefield."
        );
        addEffect(EffectSlot.ON_ATTACK, returnEffect);
    }
}
