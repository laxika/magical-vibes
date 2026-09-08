package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;

@CardRegistration(set = "EMN", collectorNumber = "7")
public class EternalScourge extends Card {

    public EternalScourge() {
        // You may cast this card from exile.
        addCastingOption(new ExileCast());

        // When this creature becomes the target of a spell or ability an opponent controls, exile
        // this creature.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new ExileSelfEffect());
    }
}
