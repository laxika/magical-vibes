package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "239")
@CardRegistration(set = "DGM", collectorNumber = "147")
@CardRegistration(set = "GRN", collectorNumber = "243")
@CardRegistration(set = "GRN", collectorNumber = "244")
@CardRegistration(set = "DDL", collectorNumber = "33")
@CardRegistration(set = "MM3", collectorNumber = "231")
@CardRegistration(set = "PIO", collectorNumber = "259")
@CardRegistration(set = "RVR", collectorNumber = "274")
@CardRegistration(set = "C13", collectorNumber = "280")
@CardRegistration(set = "C15", collectorNumber = "280")
public class BorosGuildgate extends Card {

    public BorosGuildgate() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {R} or {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {R} or {W}."
        ));
    }
}
