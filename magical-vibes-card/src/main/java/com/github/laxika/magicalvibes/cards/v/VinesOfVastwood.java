package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetingRestrictionToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "193")
public class VinesOfVastwood extends Card {

    public VinesOfVastwood() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{G}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantTargetingRestrictionToTargetUntilEndOfTurnEffect(
                        TargetingRestrictionEffect.opponentSpellsAndAbilities()))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new Kicked(), new BoostTargetCreatureEffect(4, 4)));
    }
}
