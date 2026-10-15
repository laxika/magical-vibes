package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RollPlanarDieWithAdvantageEffect;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "46")
@CardRegistration(set = "MOC", collectorNumber = "133")
public class IchorElixir extends Card {

    public IchorElixir() {
        addEffect(EffectSlot.STATIC, new RollPlanarDieWithAdvantageEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));
    }
}
