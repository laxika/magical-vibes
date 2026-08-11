package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "113")
public class NightscapeMaster extends Card {

    public NightscapeMaster() {
        // {U}{U}, {T}: Return target creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(ReturnToHandEffect.target()),
                "{U}{U}, {T}: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));

        // {R}{R}, {T}: This creature deals 2 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{R}",
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{R}{R}, {T}: This creature deals 2 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
