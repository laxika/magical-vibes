package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "121")
public class CarrionWall extends Card {

    public CarrionWall() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()), "{1}{B}: Regenerate Carrion Wall."));
    }
}
