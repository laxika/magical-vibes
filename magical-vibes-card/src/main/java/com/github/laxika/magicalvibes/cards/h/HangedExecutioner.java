package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "22")
public class HangedExecutioner extends Card {

    public HangedExecutioner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new ExileSelfCost(), new ExileTargetPermanentEffect()),
                "{3}{W}, Exile this creature: Exile target creature.",
                TargetFilters.creature()
        ));
    }
}
