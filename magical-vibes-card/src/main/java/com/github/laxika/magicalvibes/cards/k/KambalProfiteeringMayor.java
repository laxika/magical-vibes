package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OTJ", collectorNumber = "211")
public class KambalProfiteeringMayor extends Card {

    public KambalProfiteeringMayor() {
        addEffect(EffectSlot.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD,
                new OncePerTurnTriggerEffect(CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy()));
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));
    }
}
