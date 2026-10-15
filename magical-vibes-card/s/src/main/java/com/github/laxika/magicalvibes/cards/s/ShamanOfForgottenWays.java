package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "204")
public class ShamanOfForgottenWays extends Card {

    public ShamanOfForgottenWays() {
        // {T}: Add two mana in any combination of colors. Spend this mana only to cast creature spells.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.CREATURE_SPELL_ONLY, true)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast creature spells."
        ));

        // Formidable — {9}{G}{G}, {T}: Each player's life total becomes the number of creatures they control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{9}{G}{G}",
                List.of(new SetLifeTotalEffect(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        SetLifeTotalRecipient.EACH_PLAYER)),
                "Formidable — {9}{G}{G}, {T}: Each player's life total becomes the number of creatures they control."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
