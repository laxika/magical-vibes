package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "112")
@CardRegistration(set = "TLE", collectorNumber = "191")
public class AvatarRokuFirebender extends Card {

    public AvatarRokuFirebender() {
        addTrigger(Triggers.playerAttacks(),
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 6, false));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}{R}",
                List.of(new BoostTargetCreatureEffect(3, 0)),
                "{R}{R}{R}: Target creature gets +3/+0 until end of turn.",
                TargetFilters.creature()));
    }
}
