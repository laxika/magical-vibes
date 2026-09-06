package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "16")
public class CrownOfAwe extends Card {

    public CrownOfAwe() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED)),
                        GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect(
                                Set.of(CardColor.BLACK, CardColor.RED))
                ),
                "Sacrifice this Aura: Enchanted creature and other creatures that share a creature type with it gain protection from black and from red until end of turn."
        ));
    }
}
