package com.github.laxika.magicalvibes.cards.e;

import java.util.Set;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

/**
 * Exava, Rakdos Blood Witch — {2}{B}{R} Legendary Creature — Human Cleric 3/3.
 * <p>
 * First strike and haste are printed keywords (auto-loaded). Unleash is two static abilities
 * (CR 702.98a): the optional as-enters +1/+1 counter and "can't block as long as it has a
 * +1/+1 counter on it". The third static grants haste to each <em>other</em> creature its
 * controller controls that has a +1/+1 counter.
 */
@CardRegistration(set = "DGM", collectorNumber = "69")
public class ExavaRakdosBloodWitch extends Card {

    public ExavaRakdosBloodWitch() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.HASTE),
                GrantScope.OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
