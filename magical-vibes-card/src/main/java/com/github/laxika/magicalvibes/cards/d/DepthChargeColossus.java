package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "78")
public class DepthChargeColossus extends Card {

    public DepthChargeColossus() {
        addPrototype("{4}{U}{U}", CardColor.BLUE, 6, 6);

        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}: Untap Depth Charge Colossus."
        ));
    }
}
