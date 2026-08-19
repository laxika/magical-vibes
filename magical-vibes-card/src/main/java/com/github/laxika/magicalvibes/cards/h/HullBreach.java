package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "111")
public class HullBreach extends Card {

    public HullBreach() {
        setAllowSharedTargets(true);

        TargetFilter artifact = TargetFilters.artifact();
        TargetFilter enchantment = TargetFilters.enchantment();
        CardEffect destroyArtifact = new DestroyTargetPermanentEffect();
        CardEffect destroyEnchantment = new DestroyTargetPermanentEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        destroyArtifact,
                        artifact
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        destroyEnchantment,
                        enchantment
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact and target enchantment",
                        List.of(destroyArtifact, destroyEnchantment),
                        List.of(artifact, enchantment)
                )
        )));
    }
}
