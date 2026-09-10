package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "51")
public class StoneHavenMedic extends Card {

    public StoneHavenMedic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new GainLifeEffect(1)),
                "{W}, {T}: You gain 1 life."
        ));
    }
}
