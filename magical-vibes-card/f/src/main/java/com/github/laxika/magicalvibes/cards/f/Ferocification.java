package com.github.laxika.magicalvibes.cards.f;

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

@CardRegistration(set = "OTJ", collectorNumber = "123")
public class Ferocification extends Card {

    public Ferocification() {
        var creatureYouControl = TargetFilters.creatureYouControl();
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gets +2/+0 until end of turn",
                        new BoostTargetCreatureEffect(2, 0), creatureYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gains menace and haste until end of turn",
                        List.of(
                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                        creatureYouControl)
        )));
    }
}
