package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "8")
@CardRegistration(set = "MKC", collectorNumber = "319")
public class SophiaDoggedDetective extends Card {

    private static final CreateTokenEffect TINY = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Tiny",
            2,
            2,
            CardColor.GREEN,
            null,
            List.of(CardSubtype.DOG, CardSubtype.DETECTIVE),
            Set.of(Keyword.TRAMPLE),
            Set.of(),
            false,
            false,
            Map.of(),
            List.of(),
            false,
            false,
            true,
            0,
            Set.of(),
            Set.of(CardSupertype.LEGENDARY));

    public SophiaDoggedDetective() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, TINY);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsTokenPredicate())),
                                "an artifact token",
                                false),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.DOG))))),
                "{1}, Sacrifice an artifact token: Put a +1/+1 counter on each Dog you control."));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DOG),
                        SequenceEffect.of(
                                CreateTokenEffect.ofFoodToken(1),
                                CreateTokenEffect.ofClueToken(1))));
    }
}
