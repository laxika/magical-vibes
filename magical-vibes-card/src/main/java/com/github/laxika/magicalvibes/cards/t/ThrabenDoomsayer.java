package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "25")
public class ThrabenDoomsayer extends Card {

    public ThrabenDoomsayer() {
        // {T}: Create a 1/1 white Human creature token.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new CreateTokenEffect("Human", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of())),
                "{T}: Create a 1/1 white Human creature token."));

        // Fateful hour — As long as you have 5 or less life, other creatures you control get +2/+2.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerLifeAtMost(5), new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES)));
    }
}
