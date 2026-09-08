package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "233")
public class CityscapeLeveler extends Card {

    public CityscapeLeveler() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addEffect(EffectSlot.ON_SELF_CAST, new DestroyTargetPermanentEffect(false,
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))))
                .addEffect(EffectSlot.ON_ATTACK, new DestroyTargetPermanentEffect(false,
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))));

        addUnearth("{8}");
    }
}
