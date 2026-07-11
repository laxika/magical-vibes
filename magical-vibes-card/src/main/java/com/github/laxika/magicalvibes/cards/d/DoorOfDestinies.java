package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "143")
public class DoorOfDestinies extends Card {

    public DoorOfDestinies() {
        // As Door of Destinies enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Whenever you cast a spell of the chosen type, put a charge counter on Door of Destinies.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new ChosenSubtypeSpellCastTriggerEffect(
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)), false
        ));

        // Creatures you control of the chosen type get +1/+1 for each charge counter on Door of Destinies.
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1, CounterType.CHARGE));
    }
}
