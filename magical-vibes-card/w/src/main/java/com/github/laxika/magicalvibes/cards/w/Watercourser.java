package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "78")
@CardRegistration(set = "ORI", collectorNumber = "82")
public class Watercourser extends Card {

    public Watercourser() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, -1)), "{U}: This creature gets +1/-1 until end of turn."));
    }
}
