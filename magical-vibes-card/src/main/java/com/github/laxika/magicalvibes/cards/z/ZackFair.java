package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutSacrificedPermanentCountersAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "45")
@CardRegistration(set = "FIN", collectorNumber = "435")
@CardRegistration(set = "FIN", collectorNumber = "580")
public class ZackFair extends Card {

    public ZackFair() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        SacrificeSelfCost.recordingPermanentSnapshot(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET),
                        new PutSacrificedPermanentCountersAndAttachEquipmentEffect()),
                "{1}, Sacrifice Zack Fair: Target creature you control gains indestructible until end of turn. "
                        + "Put Zack Fair's counters on that creature and attach an Equipment that was attached "
                        + "to Zack Fair to that creature.",
                TargetFilters.creatureYouControl()));
    }
}
