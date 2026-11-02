package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "62")
public class DormantGomazoa extends Card {

    public DormantGomazoa() {
        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(DoesntUntapEffect.self());

        addTrigger(Triggers.youBecomeTargetOfSpell(),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap this creature?"));
    }
}
