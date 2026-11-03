package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "60")
public class TaigamMasterOpportunist extends Card {

    public TaigamMasterOpportunist() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new CopyTriggeringSpellEffect().withoutNewTargetChoice(),
                new ExileTriggeringSpellWithSuspendCountersEffect(4));
    }
}
