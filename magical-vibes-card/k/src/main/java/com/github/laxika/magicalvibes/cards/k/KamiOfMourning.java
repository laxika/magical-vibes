package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueGreaterThanSourceManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "14")
public class KamiOfMourning extends Card {

    public KamiOfMourning() {
        target(1, 1).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(new TriggeredAbility(
                        new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD),
                        new TriggeringCardConditionalEffect(
                                new CardManaValueGreaterThanSourceManaValuePredicate(),
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))));
    }
}
