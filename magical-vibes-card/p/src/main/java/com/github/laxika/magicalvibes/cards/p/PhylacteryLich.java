package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "110")
@CardRegistration(set = "M13", collectorNumber = "104")
@CardRegistration(set = "M19", collectorNumber = "113")
public class PhylacteryLich extends Card {

    public PhylacteryLich() {
        // "As Phylactery Lich enters, put a phylactery counter on an artifact you control."
        // This does NOT target — shroud/hexproof don't prevent it. The artifact is chosen
        // as the creature enters, not when the spell is cast (MTG rulings).
        addStaticEffect(new PutPhylacteryCounterOnTargetPermanentEffect());

        // "When you control no permanents with phylactery counters on them, sacrifice
        // Phylactery Lich." — State-triggered ability (MTG rule 603.8).
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream().noneMatch(p -> p.getCounterCount(CounterType.PHYLACTERY) > 0);
                }),
                new SacrificeSelfEffect());
    }
}
