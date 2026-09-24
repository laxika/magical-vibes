package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "254")
public class PathOfAncestry extends Card {

    public PathOfAncestry() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.PATH_OF_ANCESTRY)),
                "{T}: Add one mana of any color in your commander's color identity. When that mana is spent "
                        + "to cast a creature spell that shares a creature type with your commander, scry 1."
        ));
    }
}
