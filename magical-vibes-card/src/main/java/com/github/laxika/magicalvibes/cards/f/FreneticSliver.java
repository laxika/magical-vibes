package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "157")
public class FreneticSliver extends Card {

    public FreneticSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        ActivatedAbility ability = new ActivatedAbility(
                false,
                "{0}",
                List.of(new FlipCoinWinEffect(
                        FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false),
                        new SacrificeSelfEffect())),
                "{0}: If this permanent is on the battlefield, flip a coin. If you win the flip, "
                        + "exile this permanent and return it to the battlefield under its owner's "
                        + "control at the beginning of the next end step. If you lose the flip, "
                        + "sacrifice it."
        );

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ability,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ability,
                GrantScope.SELF,
                sliver
        ));
    }
}
