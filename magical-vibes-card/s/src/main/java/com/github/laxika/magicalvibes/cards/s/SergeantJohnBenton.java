package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "157")
@CardRegistration(set = "WHO", collectorNumber = "441")
@CardRegistration(set = "WHO", collectorNumber = "762")
@CardRegistration(set = "WHO", collectorNumber = "1032")
public class SergeantJohnBenton extends Card {

    public SergeantJohnBenton() {
        // You and the player dealt combat damage each draw that many cards.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DrawCardEffect(new EventValue()),
                new DrawCardForTargetPlayerEffect(new EventValue(), false, false)));
    }
}
