package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "161")
public class UntimelyMalfunction extends Card {

    public UntimelyMalfunction() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Change the target of target spell or ability with a single target",
                        new ChangeTargetOfTargetSpellWithSingleTargetEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryIsSingleTargetPredicate(),
                                "Target spell or ability must have a single target.")),
                new ChooseOneEffect.ChooseOneOption(
                        "One or two target creatures can't block this turn",
                        List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                        TargetFilters.creature(), null, 1, 2, false, null)
        )));
    }
}
