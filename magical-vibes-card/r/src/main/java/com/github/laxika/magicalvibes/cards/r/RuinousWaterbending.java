package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.WaterbendCostPaid;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "118")
public class RuinousWaterbending extends Card {

    public RuinousWaterbending() {
        addCastingCost(WaterbendCost.optional(4));
        addSpellEffect(new BoostAllCreaturesEffect(-2, -2));
        addSpellEffect(new ConditionalEffect(new WaterbendCostPaid(),
                new RegisterGlobalTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), new GainLifeEffect(1)))));
    }
}
