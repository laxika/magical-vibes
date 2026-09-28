package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "667")
public class TitaniumMan extends Card {

    public TitaniumMan() {
        addEffect(EffectSlot.ON_ATTACK, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Titanium Man gains flying until end of turn",
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                new ChooseOneEffect.ChooseOneOption(
                        "Titanium Man deals 1 damage to any target",
                        new DealDamageToAnyTargetEffect(1))
        )));
    }
}
