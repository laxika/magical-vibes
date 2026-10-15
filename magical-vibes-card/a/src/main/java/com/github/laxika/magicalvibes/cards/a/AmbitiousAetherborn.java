package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "72")
public class AmbitiousAetherborn extends Card {

    public AmbitiousAetherborn() {
        CreateTokenEffect servo = new CreateTokenEffect(1, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect(
                new com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield(), servo,
                new com.github.laxika.magicalvibes.model.effect.MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                        "Put a +1/+1 counter on Ambitious Aetherborn?", servo)));
    }
}
