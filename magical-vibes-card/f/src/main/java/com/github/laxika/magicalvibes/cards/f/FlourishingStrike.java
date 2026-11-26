package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "159")
public class FlourishingStrike extends Card {

    public FlourishingStrike() {
        setAllowSharedTargets(true);
        addCastingCost(EscalateCost.entwine("{2}{G}"));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Flourishing Strike deals 5 damage to target creature with flying",
                        DealDamageEffect.toTargetCreature(5),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                                "Target must be a creature with flying.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+3 until end of turn",
                        BoostEffect.target(3, 3),
                        TargetFilters.creature())
        ), false, 1, 2, false));
    }
}
