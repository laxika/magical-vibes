package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YONE", collectorNumber = "23")
public class IchorAberration extends Card {

    public IchorAberration() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourcePowerAtLeast(7),
                new CanAttackAsThoughNoDefenderEffect()));

        addEffect(EffectSlot.ON_CONTROLLER_PROLIFERATES, SequenceEffect.of(
                new PerpetuallyBoostSourceEffect(1, 1),
                new AllowCastSourceCardFromGraveyardThisTurnEffect()));
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_PROLIFERATES, SequenceEffect.of(
                new PerpetuallyBoostSourceEffect(1, 1),
                new AllowCastSourceCardFromGraveyardThisTurnEffect()));
    }
}
