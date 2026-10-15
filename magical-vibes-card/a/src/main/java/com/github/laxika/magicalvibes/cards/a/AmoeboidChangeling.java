package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "51")
@CardRegistration(set = "H09", collectorNumber = "3")
public class AmoeboidChangeling extends Card {

    public AmoeboidChangeling() {
        // {T}: Target creature gains all creature types until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.TARGET)),
                "{T}: Target creature gains all creature types until end of turn.",
                TargetFilters.creature()));

        // {T}: Target creature loses all creature types until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new LoseAllCreatureTypesEffect()),
                "{T}: Target creature loses all creature types until end of turn.",
                TargetFilters.creature()));
    }
}
