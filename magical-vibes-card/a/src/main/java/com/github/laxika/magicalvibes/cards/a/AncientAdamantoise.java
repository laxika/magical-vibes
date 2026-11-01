package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "172")
public class AncientAdamantoise extends Card {

    public AncientAdamantoise() {
        addStaticEffect(new DamageNotRemovedDuringCleanupEffect());
        addStaticEffect(new RedirectPlayerDamageToSelfEffect(true));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                CreateTokenEffect.ofTreasureToken(new Fixed(10), true)));
    }
}
