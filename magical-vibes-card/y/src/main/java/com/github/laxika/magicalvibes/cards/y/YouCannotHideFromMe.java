package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSC", collectorNumber = "360")
public class YouCannotHideFromMe extends Card {

    public YouCannotHideFromMe() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MakeCreatureUnblockableEffect());
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new TargetPlayerLifeAtMost(GameData.STARTING_LIFE_TOTAL / 2 - 1),
                        new SacrificeSelfEffect()));
    }
}
