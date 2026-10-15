package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "38")
@CardRegistration(set = "M3C", collectorNumber = "90")
public class UlamogsDreadsire extends Card {

    public UlamogsDreadsire() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessSacrificesEffect(
                        new PermanentMinManaValuePredicate(1),
                        "permanent with mana value 1 or greater"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        "Eldrazi", 10, 10, null,
                        List.of(CardSubtype.ELDRAZI), Set.of(), Set.of())),
                "{T}: Create a 10/10 colorless Eldrazi creature token."
        ));
    }
}
