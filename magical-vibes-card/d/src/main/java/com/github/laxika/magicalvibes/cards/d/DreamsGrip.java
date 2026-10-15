package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "34")
public class DreamsGrip extends Card {

    public DreamsGrip() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}"));
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target permanent",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target permanent",
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.permanent())
        )));
    }
}
