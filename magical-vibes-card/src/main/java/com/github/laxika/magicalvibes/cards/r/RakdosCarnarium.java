package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "DIS", collectorNumber = "178")
@CardRegistration(set = "DDK", collectorNumber = "74")
@CardRegistration(set = "MM2", collectorNumber = "247")
@CardRegistration(set = "GK2", collectorNumber = "77")
@CardRegistration(set = "IMA", collectorNumber = "245")
@CardRegistration(set = "2X2", collectorNumber = "329")
@CardRegistration(set = "ECC", collectorNumber = "161")
@CardRegistration(set = "C13", collectorNumber = "313")
@CardRegistration(set = "CMD", collectorNumber = "284")
public class RakdosCarnarium extends Card {

    public RakdosCarnarium() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsLandPredicate(),
                "land"
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {B}{R}."
        ));
    }
}
