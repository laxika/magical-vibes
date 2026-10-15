package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "202")
public class SunbladeElf extends Card {

    public SunbladeElf() {
        // This creature gets +1/+1 as long as you control a Plains.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)), new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {4}{W}: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
