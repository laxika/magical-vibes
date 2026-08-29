package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "37")
public class VeilOfAssimilation extends Card {

    public VeilOfAssimilation() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect())
                .addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, effect());
    }

    private SequenceEffect effect() {
        return SequenceEffect.of(
                new BoostTargetCreatureEffect(1, 1),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET));
    }
}
