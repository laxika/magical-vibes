package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsToHandSimultaneouslyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "205")
public class KhalniGem extends Card {

    public KhalniGem() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnPermanentsToHandSimultaneouslyEffect(new PermanentIsLandPredicate(), "land", 2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2)),
                "{T}: Add two mana of any one color."
        ));
    }
}
