package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "151")
public class HedgeTroll extends Card {

    public HedgeTroll() {
        // This creature gets +1/+1 as long as you control a Plains.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {W}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new RegenerateEffect()),
                "{W}: Regenerate Hedge Troll."
        ));
    }
}
