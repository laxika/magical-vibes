package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "19")
public class HeronBlessedGeist extends Card {

    public HeronBlessedGeist() {
        // {3}{W}, Exile this card from your graveyard: Create two 1/1 white Spirit creature tokens
        // with flying. Activate only if you control an enchantment and only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        CreateTokenEffect.whiteSpirit(2)
                ),
                "{3}{W}, Exile this card from your graveyard: Create two 1/1 white Spirit creature "
                        + "tokens with flying. Activate only if you control an enchantment and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                "Activate only if you control an enchantment."
        ));
    }
}
