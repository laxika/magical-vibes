package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "214")
public class LoxodonHierarch extends Card {

    public LoxodonHierarch() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{W}",
                List.of(new SacrificeSelfCost(), new RegenerateAllOwnCreaturesEffect()),
                "{G}{W}, Sacrifice Loxodon Hierarch: Regenerate each creature you control."
        ));
    }
}
