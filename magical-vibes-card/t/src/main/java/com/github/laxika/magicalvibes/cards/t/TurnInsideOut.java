package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "160")
public class TurnInsideOut extends Card {

    public TurnInsideOut() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        ManifestDreadEffect.forController()));
    }
}
