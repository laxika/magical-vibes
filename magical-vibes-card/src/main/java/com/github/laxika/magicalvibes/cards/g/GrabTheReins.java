package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "95")
public class GrabTheReins extends Card {

    public GrabTheReins() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}{R}"));
        setAllowSharedTargets(true);

        CardEffect gainControlAndHaste = SequenceEffect.of(
                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, you gain control of target creature and it gains haste",
                        gainControlAndHaste,
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice a creature. Grab the Reins deals damage equal to that creature's power to any target",
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect())
        )));
    }
}
