package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "20")
public class EtherealChampion extends Card {

    public EtherealChampion() {
        // Pay 1 life: Prevent the next 1 damage that would be dealt to this creature this turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), PreventDamageEffect.nextToSelf(1)),
                "Pay 1 life: Prevent the next 1 damage that would be dealt to Ethereal Champion this turn."));
    }
}
