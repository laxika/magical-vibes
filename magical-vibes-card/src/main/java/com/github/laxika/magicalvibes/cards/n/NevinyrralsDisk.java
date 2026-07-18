package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "338")
@CardRegistration(set = "5ED", collectorNumber = "391")
public class NevinyrralsDisk extends Card {

    public NevinyrralsDisk() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {1}, {T}: Destroy all artifacts, creatures, and enchantments.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate())))),
                "{1}, {T}: Destroy all artifacts, creatures, and enchantments."
        ));
    }
}
