package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "237")
@CardRegistration(set = "DGM", collectorNumber = "146")
@CardRegistration(set = "RNA", collectorNumber = "243")
@CardRegistration(set = "RNA", collectorNumber = "244")
public class AzoriusGuildgate extends Card {

    public AzoriusGuildgate() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {W} or {U}."
        ));
    }
}
