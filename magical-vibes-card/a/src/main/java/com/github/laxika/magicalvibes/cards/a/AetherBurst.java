package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "60")
public class AetherBurst extends Card {

    public AetherBurst() {
        targetX(TargetFilters.creature(), 100).addEffect(EffectSlot.SPELL,
                ReturnToHandEffect.targetWithCastTimeXValue(new Sum(
                        new Fixed(1),
                        new CardsInGraveyard(new CardNamedPredicate("Aether Burst"), CountScope.ANY_PLAYER, true)
                )));
    }
}
