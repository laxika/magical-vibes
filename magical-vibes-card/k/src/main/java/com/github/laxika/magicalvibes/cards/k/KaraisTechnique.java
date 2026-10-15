package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "152")
@CardRegistration(set = "TMT", collectorNumber = "244")
public class KaraisTechnique extends Card {

    public KaraisTechnique() {
        addSneak("{W}{B}");
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+3 until end of turn",
                        new BoostTargetCreatureEffect(3, 3),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -3/-3 until end of turn",
                        new BoostTargetCreatureEffect(-3, -3),
                        TargetFilters.creature())
        )));
    }
}
