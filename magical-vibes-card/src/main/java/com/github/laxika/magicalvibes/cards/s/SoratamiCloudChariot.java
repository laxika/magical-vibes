package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "160")
public class SoratamiCloudChariot extends Card {

    public SoratamiCloudChariot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{2}: Target creature you control gains flying until end of turn.",
                TargetFilters.creatureYouControl()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        PreventDamageEffect.allCombatToTargetCreatures(),
                        PreventDamageEffect.allCombatByTargetCreatures()),
                "{2}: Prevent all combat damage that would be dealt to and dealt by target creature you control this turn.",
                TargetFilters.creatureYouControl()));
    }
}
