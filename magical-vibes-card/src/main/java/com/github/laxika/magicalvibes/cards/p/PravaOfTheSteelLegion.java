package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "87")
public class PravaOfTheSteelLegion extends Card {

    public PravaOfTheSteelLegion() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(1, 4, GrantScope.ALL_OWN_CREATURES,
                        new PermanentIsTokenPredicate())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new CreateTokenEffect(
                        "Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of()
                )),
                "{3}{W}: Create a 1/1 white Soldier creature token."
        ));
    }
}
