package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "180")
public class ClashOfTheEikons extends Card {

    public ClashOfTheEikons() {
        var sagaFilter = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SAGA),
                "Target must be a Saga you control");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature an opponent controls",
                        List.of(new FightTargetsEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove a lore counter from target Saga you control",
                        new RemoveCountersFromTargetPermanentEffect(CounterType.LORE, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.SAGA)),
                        sagaFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a lore counter on target Saga you control",
                        new PutCounterOnTargetPermanentEffect(CounterType.LORE, 1),
                        sagaFilter)
        )));
    }
}
