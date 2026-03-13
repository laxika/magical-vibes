package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "35")
public class SunTitan extends Card {

    public SunTitan() {
        // Whenever Sun Titan enters the battlefield or attacks,
        // you may return target permanent card with mana value 3 or less
        // from your graveyard to the battlefield.
        MayEffect returnEffect = new MayEffect(
                new ReturnCardFromGraveyardEffect(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(3)
                        ))
                ),
                "You may return target permanent card with mana value 3 or less from your graveyard to the battlefield."
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnEffect);
        addEffect(EffectSlot.ON_ATTACK, returnEffect);
    }
}
