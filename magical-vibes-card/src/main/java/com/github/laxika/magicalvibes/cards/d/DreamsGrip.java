package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
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
                        new TapOrUntapTargetPermanentEffect(),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target permanent",
                        new TapOrUntapTargetPermanentEffect(),
                        TargetFilters.permanent())
        )));
    }
}
