package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "223")
public class InquisitivePuppet extends Card {

    public InquisitivePuppet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileSelfCost(),
                        new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.HUMAN), Set.of(), Set.of())
                ),
                "Exile this creature: Create a 1/1 white Human creature token."
        ));
    }
}
