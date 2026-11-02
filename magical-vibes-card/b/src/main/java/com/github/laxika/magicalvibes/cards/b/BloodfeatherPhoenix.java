package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "132")
@CardRegistration(set = "MOM", collectorNumber = "362")
public class BloodfeatherPhoenix extends Card {

    public BloodfeatherPhoenix() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(new TriggerCondition(Triggers.instantOrSorceryYouControlDealsDamageToOpponentOrBattle(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayPayManaEffect("{R}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .battlefieldEffectGrants(List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)))
                                .battlefieldEffectGrantDuration(EffectDuration.UNTIL_END_OF_TURN)
                                .build(),
                        "Pay {R} to return Bloodfeather Phoenix from your graveyard to the battlefield?"));
    }
}
