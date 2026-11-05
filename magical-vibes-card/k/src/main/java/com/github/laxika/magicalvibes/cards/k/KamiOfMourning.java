package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueGreaterThanSourceCardManaValuePredicate;

@CardRegistration(set = "YNEO", collectorNumber = "14")
public class KamiOfMourning extends Card {

    public KamiOfMourning() {
        target(1, 1).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(new TriggeredAbility(
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentManaValueGreaterThanSourceCardManaValuePredicate(),
                                ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))));
    }
}
