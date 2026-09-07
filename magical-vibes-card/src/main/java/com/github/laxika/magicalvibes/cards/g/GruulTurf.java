package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "158")
public class GruulTurf extends Card {

    public GruulTurf() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsLandPredicate(),
                "land"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {R}{G}."
        ));
    }
}
