package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "ALA", collectorNumber = "185")
@CardRegistration(set = "ALA", collectorNumber = "250")
@CardRegistration(set = "V11", collectorNumber = "10")
@CardRegistration(set = "2X2", collectorNumber = "268")
@CardRegistration(set = "CMM", collectorNumber = "352")
@CardRegistration(set = "CMM", collectorNumber = "589")
public class RafiqOfTheMany extends Card {

    public RafiqOfTheMany() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. Same wiring as Akrasan Squire — Triggers.creatureYouControlAttacks() records the lone attacker
        // as the non-targeting trigger target and AttacksAlone restricts it to solo attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        // Whenever a creature you control attacks alone, it gains double strike until end of turn.
        // GrantScope.TARGET grants to the recorded lone attacker, same trigger target as the boost.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
    }
}
