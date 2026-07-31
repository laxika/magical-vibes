package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "111")
public class WanderingMage extends Card {

    public WanderingMage() {
        // {W}, Pay 1 life: Prevent the next 2 damage that would be dealt to target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new PayLifeCost(1), PreventDamageEffect.nextToTargetCreature(2)),
                "{W}, Pay 1 life: Prevent the next 2 damage that would be dealt to target creature this turn.",
                TargetFilters.creature()
        ));

        // {U}: Prevent the next 1 damage that would be dealt to target Cleric or Wizard creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(PreventDamageEffect.nextToTargetCreature(1)),
                "{U}: Prevent the next 1 damage that would be dealt to target Cleric or Wizard creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.CLERIC, CardSubtype.WIZARD))
                        )),
                        "Target must be a Cleric or Wizard creature")
        ));

        // {B}, Put a -1/-1 counter on a creature you control:
        // Prevent the next 2 damage that would be dealt to target player or planeswalker this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        PreventDamageEffect.nextToTargetPlayerOrPlaneswalker(2)
                ),
                "{B}, Put a -1/-1 counter on a creature you control: Prevent the next 2 damage that would be "
                        + "dealt to target player or planeswalker this turn."
        ));
    }
}
