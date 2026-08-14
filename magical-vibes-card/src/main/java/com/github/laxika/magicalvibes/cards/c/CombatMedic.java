package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ATH", collectorNumber = "4")
public class CombatMedic extends Card {

    public CombatMedic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{1}{W}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
