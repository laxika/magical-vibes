package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "198")
public class SomberwaldAlpha extends Card {

    public SomberwaldAlpha() {
        // Whenever a creature you control becomes blocked, it gets +1/+1 until end of turn.
        // "it" is the blocked creature, set as the non-targeting source of the trigger.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));

        // {1}{G}: Target creature you control gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "{1}{G}: Target creature you control gains trample until end of turn.",
                TargetFilters.creatureYouControl()));
    }
}
