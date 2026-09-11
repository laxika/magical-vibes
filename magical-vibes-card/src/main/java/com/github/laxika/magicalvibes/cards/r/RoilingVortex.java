package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "156")
public class RoilingVortex extends Card {

    public RoilingVortex() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER));

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                List.of(new DealDamageToPlayersEffect(5, DamageRecipient.TRIGGERING_PLAYER)),
                new NotCondition(new SpellManaSpentAtLeast(1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new OpponentsCantGainLifeThisTurnEffect()),
                "{R}: Your opponents can't gain life this turn."
        ));
    }
}
