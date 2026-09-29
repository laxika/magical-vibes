package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "53")
public class LavabrinkFloodgates extends Card {

    public LavabrinkFloodgates() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED, 2)),
                "{T}: Add {R}{R}."
        ));

        ChooseOneEffect putOrRemove = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a doom counter on Lavabrink Floodgates",
                        List.of(new PutCountersOnSelfEffect(CounterType.DOOM))),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove a doom counter from Lavabrink Floodgates",
                        List.of(new RemoveCounterFromSourceEffect(CounterType.DOOM, 1)))
        ), true, 0, 1, false);

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, SequenceEffect.of(
                putOrRemove,
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.DOOM),
                        SequenceEffect.of(
                                new SacrificeSelfEffect(),
                                new MassDamageEffect(6)))));
    }
}
