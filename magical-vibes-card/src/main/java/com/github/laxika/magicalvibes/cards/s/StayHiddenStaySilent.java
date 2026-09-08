package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleEnchantedPermanentIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "74")
public class StayHiddenStaySilent extends Card {

    public StayHiddenStaySilent() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{U}",
                List.of(new ShuffleEnchantedPermanentIntoOwnerLibraryEffect(),
                        ManifestDreadEffect.forController()),
                "{4}{U}{U}: Shuffle enchanted creature into its owner's library, then manifest dread. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
