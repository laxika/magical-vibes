package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "244")
@CardRegistration(set = "5ED", collectorNumber = "317")
public class PradeshGypsies extends Card {

    public PradeshGypsies() {
        addActivatedAbility(new ActivatedAbility(true, "{1}{G}", List.of(new BoostTargetCreatureEffect(-2, 0)),
                "{1}{G}, {T}: Target creature gets -2/-0 until end of turn."));
    }
}
