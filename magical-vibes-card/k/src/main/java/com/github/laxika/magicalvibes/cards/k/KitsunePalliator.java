package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "14")
public class KitsunePalliator extends Card {

    public KitsunePalliator() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(PreventDamageEffect.nextToEachCreatureAndPlayer(1)),
                "{T}: Prevent the next 1 damage that would be dealt to each creature and each player this turn."));
    }
}
