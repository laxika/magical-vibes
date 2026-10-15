package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "93")
public class SimicRagworm extends Card {

    public SimicRagworm() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{U}: Untap this creature."));
    }
}
