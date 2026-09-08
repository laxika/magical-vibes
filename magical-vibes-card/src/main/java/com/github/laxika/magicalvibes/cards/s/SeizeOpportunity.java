package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "119")
public class SeizeOpportunity extends Card {

    public SeizeOpportunity() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play those cards",
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Up to two target creatures each get +2/+1 until end of turn",
                        List.<CardEffect>of(new BoostTargetCreatureEffect(2, 1)),
                        TargetFilters.creature(), null, 0, 2, false, null)
        )));
    }
}
