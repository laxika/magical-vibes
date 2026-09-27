package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EntwineManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "159")
public class FlourishingStrike extends Card {

    public FlourishingStrike() {
        addEffect(EffectSlot.SPELL, new EntwineManaCost("{2}{G}"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Flourishing Strike deals 5 damage to target creature with flying",
                        new DealDamageToTargetCreatureEffect(5),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                                "Target must be a creature with flying.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+3 until end of turn",
                        new BoostTargetCreatureEffect(3, 3),
                        TargetFilters.creature())
        ), false, 1, 2, false));
    }
}
