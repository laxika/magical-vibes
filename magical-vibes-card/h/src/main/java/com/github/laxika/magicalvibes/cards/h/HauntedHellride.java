package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "208")
public class HauntedHellride extends Card {

    public HauntedHellride() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new BoostTargetCreatureEffect(1, 0))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new UntapPermanentsEffect(TapUntapScope.TARGET));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
