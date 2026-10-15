package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "40")
public class EsperBattlemage extends Card {

    public EsperBattlemage() {
        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(PreventDamageEffect.nextToController(2)),
                "{W}, {T}: Prevent the next 2 damage that would be dealt to you this turn."));

        addActivatedAbility(new ActivatedAbility(true, "{B}",
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{B}, {T}: Target creature gets -1/-1 until end of turn."));
    }
}
