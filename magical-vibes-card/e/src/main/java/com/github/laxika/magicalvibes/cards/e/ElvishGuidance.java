package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "255")
public class ElvishGuidance extends Card {

    public ElvishGuidance() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                        new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(
                                ManaColor.GREEN,
                                new PermanentCount(
                                        new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                        CountScope.ANY_PLAYER))));
    }
}
