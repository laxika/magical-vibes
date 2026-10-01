package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInExileOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControlledCreatureWithTakeoverCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "WHO", collectorNumber = "143")
@CardRegistration(set = "WHO", collectorNumber = "426")
@CardRegistration(set = "WHO", collectorNumber = "542")
@CardRegistration(set = "WHO", collectorNumber = "748")
@CardRegistration(set = "WHO", collectorNumber = "1017")
@CardRegistration(set = "WHO", collectorNumber = "1133")
public class TheMasterFormedAnew extends Card {

    public TheMasterFormedAnew() {
        addEffect(EffectSlot.ON_SELF_CAST, new MayEffect(
                new ExileControlledCreatureWithTakeoverCounterEffect(),
                "Exile a creature you control and put a takeover counter on it?"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyCreatureCardInExileOnEnterEffect());
    }
}
