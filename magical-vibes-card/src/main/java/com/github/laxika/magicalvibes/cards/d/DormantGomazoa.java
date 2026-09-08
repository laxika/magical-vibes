package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "ROE", collectorNumber = "62")
public class DormantGomazoa extends Card {

    public DormantGomazoa() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        addEffect(EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL,
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap this creature?"));
    }
}
