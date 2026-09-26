package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingMonarchPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "89")
@CardRegistration(set = "MSC", collectorNumber = "410")
public class OkoyeMightyAndAdored extends Card {

    public OkoyeMightyAndAdored() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());

        target(TargetFilters.creature()).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new TriggeringPermanentConditionalEffect(
                                        new PermanentIsAttackingMonarchPredicate(),
                                        SequenceEffect.of(
                                                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF),
                                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF))))));
    }
}
