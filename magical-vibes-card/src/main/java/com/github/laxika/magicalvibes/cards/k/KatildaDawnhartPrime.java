package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "230")
public class KatildaDawnhartPrime extends Card {

    public KatildaDawnhartPrime() {
        addEffect(EffectSlot.STATIC,
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.WEREWOLF)));

        ActivatedAbility manaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.SOURCE_PERMANENT_COLORS)),
                "{T}: Add one mana of any of this creature's colors."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                manaAbility,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}{W}",
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentIsCreaturePredicate())),
                "{4}{G}{W}, {T}: Put a +1/+1 counter on each creature you control."
        ));
    }
}
