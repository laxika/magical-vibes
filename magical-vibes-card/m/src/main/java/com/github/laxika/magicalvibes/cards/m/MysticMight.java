package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "86")
public class MysticMight extends Card {

    public MysticMight() {
        // Cumulative upkeep {1}{U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}{U}"));

        // Enchant land you control — grants "{T}: Target creature gets +2/+2 until end of turn."
        target(TargetFilters.landYouControl()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new BoostTargetCreatureEffect(2, 2)),
                        "{T}: Target creature gets +2/+2 until end of turn.",
                        TargetFilters.creature()
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));
    }
}
