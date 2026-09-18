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

@CardRegistration(set = "RAV", collectorNumber = "278")
@CardRegistration(set = "DDJ", collectorNumber = "80")
@CardRegistration(set = "MM2", collectorNumber = "243")
@CardRegistration(set = "GK1", collectorNumber = "74")
@CardRegistration(set = "IMA", collectorNumber = "236")
@CardRegistration(set = "2X2", collectorNumber = "324")
@CardRegistration(set = "C13", collectorNumber = "291")
@CardRegistration(set = "CMD", collectorNumber = "275")
public class GolgariRotFarm extends Card {

    public GolgariRotFarm() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentIsLandPredicate(), "land"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {B}{G}."
        ));
    }
}
