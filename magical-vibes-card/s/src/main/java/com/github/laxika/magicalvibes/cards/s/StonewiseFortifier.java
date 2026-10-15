package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "27")
public class StonewiseFortifier extends Card {

    public StonewiseFortifier() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new PreventAllDamageToSelfFromTargetCreatureEffect()),
                "{4}{W}: Prevent all damage that would be dealt to Stonewise Fortifier by target creature this turn.",
                TargetFilters.creature()));
    }
}
