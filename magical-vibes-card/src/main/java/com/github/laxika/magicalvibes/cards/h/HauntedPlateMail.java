package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseSubtypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "212")
public class HauntedPlateMail extends Card {

    public HauntedPlateMail() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 4, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.SPIRIT), Set.of()),
                        new LoseSubtypesUntilEndOfTurnEffect(Set.of(CardSubtype.EQUIPMENT))),
                "{0}: Until end of turn, this permanent becomes a 4/4 Spirit artifact creature that's no longer an Equipment. Activate only if you control no creatures."
        ).withActivationCondition(
                new ControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                "Activate only if you control no creatures"));

        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
