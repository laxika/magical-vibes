package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M12", collectorNumber = "118")
@CardRegistration(set = "M14", collectorNumber = "122")
public class WringFlesh extends Card {

    public WringFlesh() {
        addSpellEffect(BoostEffect.target(-3, -1));
    }
}
