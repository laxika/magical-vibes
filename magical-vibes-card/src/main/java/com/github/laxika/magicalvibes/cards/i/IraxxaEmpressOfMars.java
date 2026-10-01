package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "89")
@CardRegistration(set = "WHO", collectorNumber = "694")
public class IraxxaEmpressOfMars extends Card {

    public IraxxaEmpressOfMars() {
        // Paradox — Whenever you cast a spell from anywhere other than your hand, create a 2/2 red
        // Alien Warrior creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(List.of(
                        new CreateTokenEffect(1, "Alien Warrior", 2, 2, CardColor.RED,
                                List.of(CardSubtype.ALIEN, CardSubtype.WARRIOR), Set.of(), Set.of()))));
    }
}
