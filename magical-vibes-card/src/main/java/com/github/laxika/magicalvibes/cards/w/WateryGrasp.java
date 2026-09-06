package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleEnchantedPermanentIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "82")
public class WateryGrasp extends Card {

    public WateryGrasp() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(5), new ShuffleEnchantedPermanentIntoOwnerLibraryEffect()),
                "Waterbend {5}: Enchanted creature's owner shuffles it into their library."
        ));
    }
}
