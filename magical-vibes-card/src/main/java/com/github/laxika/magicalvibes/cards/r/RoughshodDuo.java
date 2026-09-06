package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "150")
public class RoughshodDuo extends Card {

    public RoughshodDuo() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                null,
                TargetFilters.creatureYouControl(),
                null,
                false,
                false,
                null,
                0,
                4
        ));
    }
}
