package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceAuraEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "101")
public class VolrathsCurse extends Card {

    public VolrathsCurse() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                        List.of(new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent"),
                                new IgnoreSourceAuraEffectsUntilEndOfTurnEffect()),
                        "Sacrifice a permanent: Ignore this effect until end of turn. "
                                + "Only the enchanted creature's controller may activate this ability.")
                        .withActivatableByAnyPlayer()
                        .withActivatableOnlyByEnchantedPermanentController());
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                        List.of(ReturnToHandEffect.self()),
                        "{1}{U}: Return this Aura to its owner's hand."));
    }
}
