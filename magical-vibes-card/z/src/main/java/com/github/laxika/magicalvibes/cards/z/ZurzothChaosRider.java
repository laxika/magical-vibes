package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "278")
public class ZurzothChaosRider extends Card {

    public ZurzothChaosRider() {
        List<Ability> devilTokenEffects = List.of(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(1)));
        CreateTokenEffect devilToken = new CreateTokenEffect(
                1, "Devil", 1, 1, CardColor.RED,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects);

        addTrigger(Triggers.opponentDrawsNthCardEachTurn(1),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerIsActive())),
                devilToken);

        addTrigger(Triggers.youAttackPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.DEVIL))),
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new DrawCardForDefendingPlayerEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER, true),
                        new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER, true)));
    }
}
