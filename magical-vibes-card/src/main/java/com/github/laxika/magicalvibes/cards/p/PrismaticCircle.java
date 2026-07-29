package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "34")
public class PrismaticCircle extends Card {

    public PrismaticCircle() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYouOfChosenColor()),
                "The next time a source of your choice of the chosen color would deal damage to you this turn, prevent that damage."
        ));
    }
}
