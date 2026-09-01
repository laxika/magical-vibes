package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "94")
public class ErebossIntervention extends Card {

    public ErebossIntervention() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -X/-X until end of turn. You gain X life",
                        List.of(
                                new BoostTargetCreatureEffect(new Scaled(new XValue(), -1),
                                        new Scaled(new XValue(), -1)),
                                new GainLifeEffect(new XValue())
                        ),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile up to twice X target cards from graveyards",
                        ExileCardsFromGraveyardEffect.upToXTimesCardsFromAllGraveyards(2))
        )));
    }
}
