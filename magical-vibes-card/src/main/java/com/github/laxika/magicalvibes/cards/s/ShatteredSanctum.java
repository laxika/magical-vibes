package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessManyLandsEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "260")
@CardRegistration(set = "SOS", collectorNumber = "303")
public class ShatteredSanctum extends Card {

    public ShatteredSanctum() {
        addEffect(EffectSlot.STATIC, new EntersTappedUnlessManyLandsEffect(2));

        // {T}: Add {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}."
        ));

        // {T}: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{T}: Add {B}."
        ));
    }
}
