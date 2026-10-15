package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtByTargetPlayerSorceryThisTurn;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSorceryCasterForBackdraftEffect;

@CardRegistration(set = "LEG", collectorNumber = "132")
public class Backdraft extends Card {

    public Backdraft() {
        addEffect(EffectSlot.SPELL, new ChooseSorceryCasterForBackdraftEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new Divided(new DamageDealtByTargetPlayerSorceryThisTurn(), 2),
                DamageRecipient.CHOSEN_PLAYER));
    }
}
