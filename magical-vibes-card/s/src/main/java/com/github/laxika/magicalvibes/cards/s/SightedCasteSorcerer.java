package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "26")
public class SightedCasteSorcerer extends Card {

    public SightedCasteSorcerer() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. Triggers.creatureYouControlAttacks() records the attacker as the trigger's target;
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        // {U}: This creature gains shroud until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)),
                "{U}: This creature gains shroud until end of turn."));
    }
}
