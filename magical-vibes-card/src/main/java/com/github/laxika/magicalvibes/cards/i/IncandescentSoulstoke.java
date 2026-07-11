package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "178")
public class IncandescentSoulstoke extends Card {

    public IncandescentSoulstoke() {
        // Other Elemental creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELEMENTAL))));

        // {1}{R}, {T}: You may put an Elemental creature card from your hand onto the battlefield.
        // That creature gains haste until end of turn. Sacrifice it at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.ELEMENTAL))),
                                "Elemental creature", false, false, true, true),
                        "Put an Elemental creature card from your hand onto the battlefield?"
                )),
                "{1}{R}, {T}: You may put an Elemental creature card from your hand onto the battlefield. "
                        + "That creature gains haste until end of turn. Sacrifice it at the beginning of the next end step."
        ));
    }
}
