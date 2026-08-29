package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "223")
public class ReinforcedBulwark extends Card {

    public ReinforcedBulwark() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(PreventDamageEffect.nextToController(1)),
                "{T}: Prevent the next 1 damage that would be dealt to you this turn."));
    }
}
