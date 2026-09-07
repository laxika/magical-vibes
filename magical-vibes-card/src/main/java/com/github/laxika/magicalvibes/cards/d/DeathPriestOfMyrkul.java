package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "95")
public class DeathPriestOfMyrkul extends Card {

    public DeathPriestOfMyrkul() {
        // Skeletons, Vampires, and Zombies you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.SKELETON, CardSubtype.VAMPIRE, CardSubtype.ZOMBIE))));

        // At the beginning of your end step, if a creature died this turn, you may pay {1}.
        // If you do, create a 1/1 black Skeleton creature token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(new Morbid(),
                new MayPayManaEffect("{1}",
                        new CreateTokenEffect("Skeleton", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.SKELETON), Set.of(), Set.of()),
                        "Pay {1} to create a Skeleton token?")));
    }
}
