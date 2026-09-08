package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfCombatDamageDealersToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "204")
public class KaitoDancingShadow extends Card {

    public KaitoDancingShadow() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new MayEffect(
                                new ReturnOneOfCombatDamageDealersToHandThenEffect(
                                        new MayEffect(new GrantExtraLoyaltyActivationToSourceEffect(),
                                                "Activate Kaito's loyalty abilities twice this turn?"),
                                        "a creature that dealt combat damage"),
                                "Return one of them to its owner's hand?"),
                        false,
                        true));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new LockTargetPermanentEffect(true, true, false, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Up to one target creature can't attack or block until your next turn.",
                TargetFilters.creature(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(1)),
                "0: Draw a card."
        ));

        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2))
        );
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Drone", 2, 2, null,
                        List.of(CardSubtype.DRONE),
                        Set.of(Keyword.DEATHTOUCH),
                        Set.of(CardType.ARTIFACT),
                        tokenEffects)),
                "\u22122: Create a 2/2 colorless Drone artifact creature token with deathtouch and \"When this token leaves the battlefield, each opponent loses 2 life and you gain 2 life.\""
        ));
    }
}
