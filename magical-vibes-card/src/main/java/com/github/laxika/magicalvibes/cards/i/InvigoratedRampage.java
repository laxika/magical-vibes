package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "86")
public class InvigoratedRampage extends Card {

    public InvigoratedRampage() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +4/+0 and gains trample until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(4, 0),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Two target creatures each get +2/+0 and gain trample until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(2, 0),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        TargetFilters.creature(), null, 2, 2, false, null)
        )));
    }
}
