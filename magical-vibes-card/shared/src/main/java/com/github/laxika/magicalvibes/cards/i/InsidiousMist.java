package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class InsidiousMist extends Card {

    public InsidiousMist() {
        addStaticEffect(new CantBlockEffect());
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.attacksUnblocked(),
                new MayPayManaEffect("{2}{B}", new TransformSelfEffect(),
                        "Pay {2}{B} to transform this creature?"));
    }
}
