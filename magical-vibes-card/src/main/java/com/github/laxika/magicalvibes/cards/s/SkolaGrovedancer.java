package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "202")
public class SkolaGrovedancer extends Card {

    public SkolaGrovedancer() {
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{2}{G}: Mill a card."
        ));
    }
}
