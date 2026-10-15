package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "37")
public class SpectralShift extends Card {

    public SpectralShift() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Change the text of target spell or permanent by replacing all instances of one basic land type with another",
                        new ChangeColorTextEffect(false, true, true), TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Change the text of target spell or permanent by replacing all instances of one color word with another",
                        new ChangeColorTextEffect(true, false, true), TargetFilters.permanent())
        )));
    }
}
