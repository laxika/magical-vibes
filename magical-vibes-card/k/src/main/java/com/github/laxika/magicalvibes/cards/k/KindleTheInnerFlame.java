package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "147")
public class KindleTheInnerFlame extends Card {

    public KindleTheInnerFlame() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                        false, false, null, Set.of(Keyword.HASTE), false,
                        Map.of(EffectSlot.END_STEP_TRIGGERED, List.of(new SacrificeSelfEffect()))));
        addCastingOption(new FlashbackCast("{1}{R}"));
        addCastingCost(new BeholdCost(CardSubtype.ELEMENTAL, 3));
    }
}
