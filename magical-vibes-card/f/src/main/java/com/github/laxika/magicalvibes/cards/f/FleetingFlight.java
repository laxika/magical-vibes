package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "13")
public class FleetingFlight extends Card {

    public FleetingFlight() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()))
                .addSpellEffect(PreventDamageEffect.allCombatToTargetCreatures());
    }
}
