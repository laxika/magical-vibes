package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "SLD", collectorNumber = "2175")
public class JawsRelentlessPredator extends Card {

    public JawsRelentlessPredator() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                CreateTokenEffect.ofBloodToken(new EventValue()));
        addEffect(EffectSlot.ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
