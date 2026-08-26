package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "188")
public class DrownInTheLoch extends Card {

    public DrownInTheLoch() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell with mana value less than or equal to the number of cards in its controller's graveyard",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryManaValueAtMostControllerGraveyardCountPredicate(),
                                "Target spell's mana value must not exceed the number of cards in its controller's graveyard.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature with mana value less than or equal to the number of cards in its controller's graveyard",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentManaValueAtMostControllerGraveyardCountPredicate())),
                                "Target must be a creature whose mana value does not exceed the number of cards in its controller's graveyard."))
        )));
    }
}
