package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "244")
@CardRegistration(set = "DGM", collectorNumber = "153")
@CardRegistration(set = "RNA", collectorNumber = "252")
@CardRegistration(set = "RNA", collectorNumber = "253")
public class OrzhovGuildgate extends Card {

    public OrzhovGuildgate() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {W} or {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));
    }
}
