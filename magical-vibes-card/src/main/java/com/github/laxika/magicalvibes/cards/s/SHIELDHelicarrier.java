package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "249")
public class SHIELDHelicarrier extends Card {

    public SHIELDHelicarrier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(6), AnimatePermanentsEffect.crew()),
                "Crew 6"
        ));
    }
}
