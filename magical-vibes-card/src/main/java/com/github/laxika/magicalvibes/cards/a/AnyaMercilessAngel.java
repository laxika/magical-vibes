package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithLifeAtMost;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "C15", collectorNumber = "41")
public class AnyaMercilessAngel extends Card {

    public AnyaMercilessAngel() {
        int threshold = GameData.STARTING_LIFE_TOTAL / 2 - 1;
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(new OpponentsWithLifeAtMost(threshold), 3),
                new Scaled(new OpponentsWithLifeAtMost(threshold), 3),
                EffectDuration.CONTINUOUS));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnOpponentLifeAtMost(threshold),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
