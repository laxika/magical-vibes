package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "95")
@CardRegistration(set = "C16", collectorNumber = "126")
public class GrabTheReins extends Card {

    public GrabTheReins() {
        addCastingCost(new EscalateManaCost("{2}{R}"));
        setAllowSharedTargets(true);

        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, you gain control of target creature and it gains haste",
                        List.of(
                                new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                                new GrantKeywordEffect(Keyword.HASTE, Subjects.target())),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice a creature. Grab the Reins deals damage equal to that creature's power to any target",
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                                new DealDamageToAnyTargetEffect(AmountThisWay.sacrificedPower())),
                        null)
        )));
    }
}
