package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "278")
public class ZurzothChaosRider extends Card {

    public ZurzothChaosRider() {
        Map<EffectSlot, CardEffect> devilTokenEffects = Map.of(
                EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        CreateTokenEffect devilToken = new CreateTokenEffect(
                1, "Devil", 1, 1, CardColor.RED,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects);

        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(1,
                        new ConditionalEffect(new NotCondition(new TargetPlayerIsActive()), devilToken)));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.DEVIL)),
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER, true),
                                new DrawCardForDefendingPlayerEffect(),
                                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER, true))));
    }
}
