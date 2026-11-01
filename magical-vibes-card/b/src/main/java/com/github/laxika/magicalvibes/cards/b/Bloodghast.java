package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "83")
@CardRegistration(set = "DFT", collectorNumber = "77")
@CardRegistration(set = "IMA", collectorNumber = "82")
@CardRegistration(set = "SLD", collectorNumber = "6")
@CardRegistration(set = "SLD", collectorNumber = "2351")
@CardRegistration(set = "HA7", collectorNumber = "9")
@CardRegistration(set = "SOC", collectorNumber = "210")
@CardRegistration(set = "LCC", collectorNumber = "184")
public class Bloodghast extends Card {

    public Bloodghast() {
        addStaticEffect(new CantBlockEffect());
        addStaticEffect(new ConditionalEffect(
                new AnOpponentLifeAtMost(10),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                        "Return Bloodghast from your graveyard to the battlefield?"));
    }
}
