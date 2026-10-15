package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "92")
@CardRegistration(set = "FIC", collectorNumber = "182")
public class SephirothFallenHero extends Card {

    private static final PermanentAllOfPredicate MODIFIED_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsModifiedPredicate()));

    public SephirothFallenHero() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.CELL),
                        "Put a cell counter on target creature?"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new SetAllOwnCreaturesBasePowerToughnessEffect(7, 5,
                                new PermanentIsModifiedPredicate()));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificePermanentCost(MODIFIED_CREATURE, "Sacrifice a modified creature"),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()),
                "{3}, Sacrifice a modified creature: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
