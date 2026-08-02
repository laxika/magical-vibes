package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "33")
public class OrimSamiteHealer extends Card {

    public OrimSamiteHealer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(PreventDamageEffect.nextToAny(3)), "{T}: Prevent the next 3 damage that would be dealt to any target this turn."));
    }
}
