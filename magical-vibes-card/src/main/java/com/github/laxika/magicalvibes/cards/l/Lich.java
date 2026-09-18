package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeNontokenPermanentsOrLoseGameEffect;

@CardRegistration(set = "ME4", collectorNumber = "89")
public class Lich extends Card {

    public Lich() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LoseLifeEffect(new ControllerLifeTotal(), LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());
        addEffect(EffectSlot.STATIC, new NefariousLichLifeGainReplacementEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                new SacrificeNontokenPermanentsOrLoseGameEffect());
    }
}
