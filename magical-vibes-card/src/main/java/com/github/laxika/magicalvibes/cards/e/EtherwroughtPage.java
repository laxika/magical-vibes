package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "108")
public class EtherwroughtPage extends Card {

    public EtherwroughtPage() {
        // "At the beginning of your upkeep, choose one — You gain 2 life. / Surveil 1. /
        // Each opponent loses 1 life." A triggered modal ability: the mode is picked as the
        // ability resolves (ChooseOneEffectHandler → ChooseModeChoice) since there is no cast.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("You gain 2 life.", new GainLifeEffect(2)),
                new ChooseOneEffect.ChooseOneOption("Surveil 1.", new SurveilEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Each opponent loses 1 life.",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT))
        )));
    }
}
