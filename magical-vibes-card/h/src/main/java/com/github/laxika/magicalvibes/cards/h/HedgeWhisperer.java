package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "165")
public class HedgeWhisperer extends Card {

    public HedgeWhisperer() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(
                        new CollectEvidenceCost(4),
                        new AnimatePermanentsEffect(
                                5, 5,
                                List.of(CardSubtype.PLANT, CardSubtype.BOAR),
                                Set.of(Keyword.HASTE),
                                CardColor.GREEN,
                                Set.of(),
                                GrantScope.TARGET,
                                EffectDuration.WHILE_SOURCE_REMAINS_TAPPED)
                ),
                "{3}{G}, {T}, Collect evidence 4: Target land you control becomes a 5/5 green Plant Boar creature with haste for as long as this creature remains tapped. It's still a land. Activate only as a sorcery.",
                TargetFilters.landYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
