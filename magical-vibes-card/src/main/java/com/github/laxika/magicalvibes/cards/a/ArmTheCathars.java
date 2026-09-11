package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturesByPositionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "3")
public class ArmTheCathars extends Card {

    public ArmTheCathars() {
        target(TargetFilters.creature(), 1, 3)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreaturesByPositionEffect(List.of(
                        new BoostTargetCreatureEffect(3, 3),
                        new BoostTargetCreatureEffect(2, 2),
                        new BoostTargetCreatureEffect(1, 1))))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET));
    }
}
