package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.condition.FullParty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "5")
public class ArchpriestOfIona extends Card {

    public ArchpriestOfIona() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(new PartySize(), new Fixed(2)));
        target(TargetFilters.creature())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new FullParty()),
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(1, 1),
                                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)));
    }
}
