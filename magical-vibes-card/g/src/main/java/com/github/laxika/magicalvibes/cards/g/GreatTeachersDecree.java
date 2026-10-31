package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "DTK", collectorNumber = "21")
@CardRegistration(set = "IMA", collectorNumber = "21")
public class GreatTeachersDecree extends Card {

    public GreatTeachersDecree() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 1));
    }
}
