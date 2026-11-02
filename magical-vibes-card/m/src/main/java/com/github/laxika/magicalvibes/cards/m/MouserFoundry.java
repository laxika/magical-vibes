package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "96")
public class MouserFoundry extends Card {

    public MouserFoundry() {
        var robotToken = new CreateTokenEffect(
                1, "Robot", 1, 1, null, List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), robotToken);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), robotToken);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{4}{R}, Sacrifice this artifact: It deals 3 damage to target creature.",
                TargetFilters.creature()));
    }
}
