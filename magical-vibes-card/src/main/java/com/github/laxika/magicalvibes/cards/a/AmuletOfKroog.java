package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "347")
public class AmuletOfKroog extends Card {

    public AmuletOfKroog() {
        // {2}, {T}: Prevent the next 1 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PreventDamageToTargetEffect(1)),
                "{2}, {T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
