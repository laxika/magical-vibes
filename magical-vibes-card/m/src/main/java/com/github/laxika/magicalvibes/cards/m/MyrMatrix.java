package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "132")
public class MyrMatrix extends Card {

    public MyrMatrix() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MYR)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new CreateTokenEffect(
                        "Myr", 1, 1, null,
                        List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT))),
                "{5}: Create a 1/1 colorless Myr artifact creature token."
        ));
    }
}
