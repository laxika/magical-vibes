package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "114")
@CardRegistration(set = "WHO", collectorNumber = "403")
@CardRegistration(set = "WHO", collectorNumber = "719")
@CardRegistration(set = "WHO", collectorNumber = "994")
public class TheBeastDeathlessPrince extends Card {

    public TheBeastDeathlessPrince() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.youCastThisSpell(),
                        new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                        new UntapPermanentsEffect(Subjects.target()),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(new EnterWithCountersEffect(CounterType.STUN, new Fixed(6)));
        addTrigger(Triggers.creatureDealsCombatDamageToItsOwner(), SequenceEffect.of(
                new UntapPermanentsEffect(Subjects.source()),
                new DrawCardEffect(1)));
    }
}
