package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "117")
public class StreetRiot extends Card {

    public StreetRiot() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), GrantScope.OWN_CREATURES)));
    }
}
