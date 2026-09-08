package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "175")
public class PredatoryUrge extends Card {

    public PredatoryUrge() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new SourceFightsTargetCreatureEffect()),
                        "{T}: This creature deals damage equal to its power to target creature. "
                                + "That creature deals damage equal to its power to this creature.",
                        TargetFilters.creature()
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
