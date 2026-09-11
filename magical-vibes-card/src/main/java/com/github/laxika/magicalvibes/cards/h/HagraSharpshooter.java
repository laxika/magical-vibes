package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creature;

@CardRegistration(set = "BFZ", collectorNumber = "113")
public class HagraSharpshooter extends Card {

    public HagraSharpshooter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{4}{B}: Target creature gets -1/-1 until end of turn.",
                creature()
        ));
    }
}
