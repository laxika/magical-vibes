package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "181")
public class EngineeredMight extends Card {

    public EngineeredMight() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +5/+5 and gains trample until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(5, 5),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+2 and gain vigilance until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(2, 2),
                                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES)))
        )));
    }
}
