package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "95")
public class HarborBandit extends Card {

    public HarborBandit() {
        // This creature gets +1/+1 as long as you control an Island.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {1}{U}: This creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{1}{U}: Harbor Bandit can't be blocked this turn."
        ));
    }
}
