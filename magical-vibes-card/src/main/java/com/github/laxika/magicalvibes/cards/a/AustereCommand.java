package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "3")
public class AustereCommand extends Card {

    public AustereCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all artifacts",
                        new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all enchantments",
                        new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures with mana value 3 or less",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentMaxManaValuePredicate(3))))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures with mana value 4 or greater",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentMinManaValuePredicate(4)))))
        ), 2));
    }
}
