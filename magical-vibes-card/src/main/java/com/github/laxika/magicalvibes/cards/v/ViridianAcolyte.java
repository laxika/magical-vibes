package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "89")
public class ViridianAcolyte extends Card {

    public ViridianAcolyte() {
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new AwardAnyColorManaEffect()), "{1}, {T}: Add one mana of any color."));
    }
}
