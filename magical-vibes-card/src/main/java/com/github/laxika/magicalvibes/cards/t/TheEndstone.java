package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "240")
public class TheEndstone extends Card {

    public TheEndstone() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(1))
        ));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new DrawCardEffect(1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new SetLifeTotalEffect(new HalvedRoundedUp(new Fixed(GameData.STARTING_LIFE_TOTAL))));
    }
}
