package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "50")
public class DrownyardAmalgam extends Card {

    public DrownyardAmalgam() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.TARGET_PLAYER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{2}{U}: This creature can't be blocked this turn."
        ));
    }
}
