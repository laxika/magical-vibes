package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C17", collectorNumber = "15")
public class BoneyardScourge extends Card {

    public BoneyardScourge() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new MayPayManaEffect("{1}{B}",
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                                "Pay {1}{B} to return Boneyard Scourge from your graveyard to the battlefield?")));
    }
}
