package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "371")
@CardRegistration(set = "ECC", collectorNumber = "61")
public class FesteringThicket extends Card {

    public FesteringThicket() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {B} or {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
