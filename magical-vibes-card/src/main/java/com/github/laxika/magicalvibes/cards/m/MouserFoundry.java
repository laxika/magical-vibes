package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "96")
public class MouserFoundry extends Card {

    public MouserFoundry() {
        var robotToken = new CreateTokenEffect(
                1, "Robot", 1, 1, null, List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, robotToken);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, robotToken);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{4}{R}, Sacrifice this artifact: It deals 3 damage to target creature.",
                TargetFilters.creature()));
    }
}
