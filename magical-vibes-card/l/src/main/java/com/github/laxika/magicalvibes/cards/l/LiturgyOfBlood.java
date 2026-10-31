package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "104")
public class LiturgyOfBlood extends Card {

    public LiturgyOfBlood() {
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new AwardManaEffect(ManaColor.BLACK, 3));
    }
}
