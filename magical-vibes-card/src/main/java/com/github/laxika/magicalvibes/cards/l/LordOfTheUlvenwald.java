package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

public class LordOfTheUlvenwald extends Card {

    public LordOfTheUlvenwald() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF))));
        addEffect(EffectSlot.ON_ATTACK, addManaEffect());
    }

    private static ChooseOneEffect addManaEffect() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Add {R}",
                        new AwardPersistentManaEffect(ManaColor.RED, new Fixed(1))),
                new ChooseOneEffect.ChooseOneOption("Add {G}",
                        new AwardPersistentManaEffect(ManaColor.GREEN, new Fixed(1)))
        ));
    }
}
