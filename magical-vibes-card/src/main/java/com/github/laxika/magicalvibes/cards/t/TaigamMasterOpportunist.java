package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "60")
public class TaigamMasterOpportunist extends Card {

    public TaigamMasterOpportunist() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(
                        new CopyTriggeringSpellEffect(),
                        new ExileTriggeringSpellWithSuspendCountersEffect(4)
                )));
    }
}
