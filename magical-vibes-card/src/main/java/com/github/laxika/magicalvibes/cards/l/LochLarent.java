package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEntersTappedToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTargetPlayerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "30")
public class LochLarent extends Card {

    public LochLarent() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(
                        new ScryEffect(3),
                        new RegisterDelayedTargetPlayerSpellCastTriggerEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                List.of(
                                        new GrantEntersTappedToTriggeringCreatureSpellEffect(),
                                        new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                                CounterType.STUN, 1)),
                                true,
                                false)),
                "{1}{U}, {T}: Scry 3. Target opponent gets a one-time boon with \"When you cast a creature spell, that creature enters tapped and with a stun counter on it.\" Activate only during your turn and only once.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withMaxActivationsPerGame(1));
    }
}
