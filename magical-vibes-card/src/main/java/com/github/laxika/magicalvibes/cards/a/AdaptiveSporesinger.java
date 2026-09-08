package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "157")
public class AdaptiveSporesinger extends Card {

    public AdaptiveSporesinger() {
        TargetFilter creature = TargetFilters.creature();
        CardEffect boostAndVigilance = SequenceEffect.of(
                new BoostTargetCreatureEffect(2, 2),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)
        );

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+2 and gains vigilance until end of turn",
                        boostAndVigilance,
                        creature
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Proliferate",
                        new ProliferateEffect()
                )
        )));
    }
}
