package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "28")
public class SurvivorOfKorlis extends Card {

    public SurvivorOfKorlis() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new ScryEffect(2)
                ),
                "{1}{W}, Exile this card from your graveyard: Scry 2."
        ));
    }
}
