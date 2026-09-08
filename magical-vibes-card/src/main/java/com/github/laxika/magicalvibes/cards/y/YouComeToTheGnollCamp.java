package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "168")
public class YouComeToTheGnollCamp extends Card {

    public YouComeToTheGnollCamp() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Intimidate Them — Up to two target creatures can't block this turn",
                        List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                        TargetFilters.creature(),
                        null,
                        0,
                        2,
                        false,
                        null),
                new ChooseOneEffect.ChooseOneOption(
                        "Fend Them Off — Target creature gets +3/+1 until end of turn",
                        new BoostTargetCreatureEffect(3, 1),
                        TargetFilters.creature())
        )));
    }
}
