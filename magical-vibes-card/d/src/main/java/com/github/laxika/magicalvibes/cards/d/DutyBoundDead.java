package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "92")
public class DutyBoundDead extends Card {

    public DutyBoundDead() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. ON_ALLY_CREATURE_ATTACKS records the attacker as the trigger's
        // (non-targeting) target; AttacksAlone restricts it to lone attackers at resolution.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        addActivatedAbility(new ActivatedAbility(false, "{3}{B}", List.of(new RegenerateEffect()), "{3}{B}: Regenerate Duty-Bound Dead."));
    }
}
