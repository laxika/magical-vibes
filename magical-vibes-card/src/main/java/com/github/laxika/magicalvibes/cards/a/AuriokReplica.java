package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "138")
public class AuriokReplica extends Card {

    public AuriokReplica() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), new PreventAllDamageFromChosenSourceEffect()),
                "{W}, Sacrifice Auriok Replica: Prevent all damage a source of your choice would deal to you this turn."
        ));
    }
}
