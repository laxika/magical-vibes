package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "113")
public class RequiemMonolith extends Card {

    public RequiemMonolith() {
        GrantEffectToTargetUntilEndOfTurnEffect damageDraw = new GrantEffectToTargetUntilEndOfTurnEffect(
                EffectSlot.ON_DEALT_DAMAGE,
                SequenceEffect.of(
                        new DrawCardEffect(new EventValue()),
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        damageDraw,
                        new MayEffect(
                                new DealDamageToTargetCreatureEffect(1),
                                "Have Requiem Monolith deal 1 damage to it?",
                                null,
                                MayChoicePlayer.TARGET_PERMANENT_CONTROLLER)),
                "{T}: Until end of turn, target creature gains \"Whenever this creature is dealt damage, you draw that many cards and lose that much life.\" That creature's controller may have this artifact deal 1 damage to it. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
