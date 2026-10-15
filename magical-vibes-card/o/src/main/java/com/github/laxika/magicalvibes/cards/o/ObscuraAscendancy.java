package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SpellManaValueEqualsSourceCounters;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "207")
public class ObscuraAscendancy extends Card {

    public ObscuraAscendancy() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                List.of(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.SOUL),
                        new CreateTokenEffect("Spirit", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()))),
                new SpellManaValueEqualsSourceCounters(CounterType.SOUL, 1)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.SOUL),
                new StaticBoostEffect(3, 3, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))));
    }
}
