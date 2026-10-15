package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "147")
public class OperaLoveSong extends Card {

    public OperaLoveSong() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. You may play those cards until your next end step",
                        new ExileTopCardsMayPlayUntilNextEndStepEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "One or two target creatures each get +2/+0 until end of turn",
                        List.<CardEffect>of(new BoostTargetCreatureEffect(2, 0)),
                        TargetFilters.creature(), null, 1, 2, false, null)
        )));
    }
}
