package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "16")
public class SaltfieldRecluse extends Card {

    public SaltfieldRecluse() {
        // {T}: Target creature gets -2/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(-2, 0)),
                "{T}: Target creature gets -2/-0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
