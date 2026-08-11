package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "26")
public class ProtectiveSphere extends Card {

    public ProtectiveSphere() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new PayLifeCost(1), PreventDamageFromChosenSourceEffect.allDamageToYouOfActivationManaColor()),
                "{1}, Pay 1 life: Prevent all damage that would be dealt to you this turn by a source of your choice that shares a color with the mana spent on this activation cost."));
    }
}
