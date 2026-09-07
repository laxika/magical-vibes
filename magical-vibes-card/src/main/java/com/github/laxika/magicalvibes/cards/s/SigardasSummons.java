package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "VOW", collectorNumber = "36")
public class SigardasSummons extends Card {

    public SigardasSummons() {
        PermanentHasCountersPredicate hasPlusOnePlusOneCounters =
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE);

        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(
                4, 4, GrantScope.OWN_CREATURES, hasPlusOnePlusOneCounters));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES, hasPlusOnePlusOneCounters));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.ANGEL, GrantScope.OWN_CREATURES, false, hasPlusOnePlusOneCounters));
    }
}
