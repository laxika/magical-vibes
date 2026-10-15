package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "7")
public class DecoratedGriffin extends Card {

    public DecoratedGriffin() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(PreventDamageEffect.nextCombatToController(1)),
                "{1}{W}: Prevent the next 1 combat damage that would be dealt to you this turn."));
    }
}
