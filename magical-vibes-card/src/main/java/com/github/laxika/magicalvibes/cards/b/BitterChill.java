package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "44")
public class BitterChill extends Card {

    public BitterChill() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(new ScryEffect(1), new DrawCardEffect(1)),
                        "Pay {1} to scry 1, then draw a card?"));
    }
}
