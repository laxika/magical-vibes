package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "10")
public class KamiOfFalseHope extends Card {

    public KamiOfFalseHope() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), PreventDamageEffect.allCombat()),
                "Sacrifice Kami of False Hope: Prevent all combat damage that would be dealt this turn."
        ));
    }
}
