package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "FIN", collectorNumber = "172")
public class AncientAdamantoise extends Card {

    public AncientAdamantoise() {
        addEffect(EffectSlot.STATIC, new DamageNotRemovedDuringCleanupEffect());
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToSelfEffect(true));
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                CreateTokenEffect.ofTreasureToken(new Fixed(10), true)));
    }
}
