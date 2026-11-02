package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantAttackCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "33")
@CardRegistration(set = "ACR", collectorNumber = "134")
public class AlexiosDeimosOfKosmos extends Card {

    public AlexiosDeimosOfKosmos() {
        addStaticEffect(new MustAttackEffect());
        addStaticEffect(new CantBeSacrificedEffect());
        addStaticEffect(new CantAttackCardOwnerEffect(false));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(),
                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)
        ));
    }
}
