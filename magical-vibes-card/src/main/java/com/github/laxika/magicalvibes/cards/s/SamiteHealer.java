package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "38")
public class SamiteHealer extends Card {

    public SamiteHealer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new PreventNextDamageEffect(1)), false, "{T}: Prevent the next 1 damage that would be dealt to any target this turn."));
    }
}
