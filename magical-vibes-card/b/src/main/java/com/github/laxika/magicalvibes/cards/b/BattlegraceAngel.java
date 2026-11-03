package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "6")
@CardRegistration(set = "MM2", collectorNumber = "10")
public class BattlegraceAngel extends Card {

    public BattlegraceAngel() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Akrasan Squire — Triggers.creatureYouControlAttacks() records the lone attacker
        // as the non-targeting trigger target and AttacksAlone restricts it to solo attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        // Whenever a creature you control attacks alone, it gains lifelink until end of turn.
        // GrantScope.TARGET grants to the recorded lone attacker, same trigger target as the boost.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
