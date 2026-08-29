package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "243")
@CardRegistration(set = "DGM", collectorNumber = "150")
@CardRegistration(set = "RNA", collectorNumber = "249")
@CardRegistration(set = "RNA", collectorNumber = "250")
public class GruulGuildgate extends Card {

    public GruulGuildgate() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN))),
                "{T}: Add {R} or {G}."
        ));
    }
}
