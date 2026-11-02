package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "233")
@CardRegistration(set = "TLE", collectorNumber = "53")
public class CityscapeLeveler extends Card {

    public CityscapeLeveler() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.youCastThisSpell(), new DestroyTargetPermanentEffect(false,
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))))
                .addTrigger(Triggers.attacks(), new DestroyTargetPermanentEffect(false,
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1))));

        addUnearth("{8}");
    }
}
