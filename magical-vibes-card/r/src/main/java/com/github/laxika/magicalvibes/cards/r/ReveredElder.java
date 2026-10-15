package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "43")
public class ReveredElder extends Card {

    public ReveredElder() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(PreventDamageEffect.nextToSelf(1)),
                "{1}: Prevent the next 1 damage that would be dealt to this creature this turn."));
    }
}
