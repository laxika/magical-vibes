package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "226")
public class RakdosTheMuscle extends Card {

    public RakdosTheMuscle() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new TriggeringPermanentConditionalEffect(
                new PermanentIsCreaturePredicate(),
                new ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)
                ),
                "Sacrifice another creature: Rakdos gains indestructible until end of turn. Tap it. Activate only once each turn.",
                1
        ));
    }
}
