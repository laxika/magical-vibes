package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "109")
public class DyingToServe extends Card {

    public DyingToServe() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new OncePerTurnTriggerEffect(new CreateTokenEffect(
                        1, "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true)));
    }
}
