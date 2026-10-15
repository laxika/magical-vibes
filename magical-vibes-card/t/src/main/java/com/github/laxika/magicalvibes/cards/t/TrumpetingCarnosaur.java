package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "171")
@CardRegistration(set = "LCI", collectorNumber = "324")
public class TrumpetingCarnosaur extends Card {

    public TrumpetingCarnosaur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscoverEffect(5));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(3)),
                "{2}{R}, Discard this card: It deals 3 damage to target creature or planeswalker."
        ));
    }
}
