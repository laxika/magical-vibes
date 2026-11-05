package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "243")
public class SlagstoneRefinery extends Card {

    public SlagstoneRefinery() {
        CreateTokenEffect powerstone = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.putIntoGraveyardOrExileFromBattlefield(Triggers.NONTOKEN_ARTIFACT,
                                ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(powerstone))));
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS), powerstone);
    }
}
