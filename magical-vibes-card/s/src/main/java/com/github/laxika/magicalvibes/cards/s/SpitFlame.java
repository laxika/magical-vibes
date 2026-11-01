package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "142")
@CardRegistration(set = "M19", collectorNumber = "160")
@CardRegistration(set = "TDC", collectorNumber = "234")
@CardRegistration(set = "SCD", collectorNumber = "158")
@CardRegistration(set = "FDC", collectorNumber = "177")
public class SpitFlame extends Card {

    public SpitFlame() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(4));

        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        new MayPayManaEffect("{R}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Pay {R} to return Spit Flame from your graveyard to your hand?")));
    }
}
