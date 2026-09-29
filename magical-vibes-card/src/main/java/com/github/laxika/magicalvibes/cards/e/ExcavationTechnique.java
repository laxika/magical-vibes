package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C21", collectorNumber = "16")
public class ExcavationTechnique extends Card {

    public ExcavationTechnique() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL,
                        new DestroyTargetPermanentEffect(false, CreateTokenEffect.ofTreasureToken(1), 2, false));
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new DemonstrateEffect(), "Copy Excavation Technique?"));
    }
}
