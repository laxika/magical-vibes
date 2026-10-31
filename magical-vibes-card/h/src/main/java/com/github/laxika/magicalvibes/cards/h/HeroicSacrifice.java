package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "18")
@CardRegistration(set = "MSC", collectorNumber = "310")
public class HeroicSacrifice extends Card {

    public HeroicSacrifice() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new RedirectYourDamageToTargetCreatureThisTurnEffect())
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        SequenceEffect.upToOneTarget(
                                MoveDyingSourceCountersToTargetCreatureEffect.alwaysTriggers(true),
                                new DrawCardEffect(1))));
    }
}
