package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AddNotedManaEffect;
import com.github.laxika.magicalvibes.model.effect.NoteManaSpentForActivationEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "326")
public class JeweledAmulet extends Card {

    public JeweledAmulet() {
        // {1}, {T}: Put a charge counter on this artifact. Note the type of mana spent to pay this
        // activation cost. Activate only if there are no charge counters on this artifact.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new NoteManaSpentForActivationEffect()
                ),
                "{1}, {T}: Put a charge counter on this artifact. Note the type of mana spent to pay this activation cost. Activate only if there are no charge counters on this artifact.")
                .withActivationCondition(new NotCondition(new SourceCounterThreshold(1, CounterType.CHARGE)),
                        "no charge counters on Jeweled Amulet"));

        // {T}, Remove a charge counter from this artifact: Add one mana of this artifact's last
        // noted type.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new AddNotedManaEffect()
                ),
                "{T}, Remove a charge counter from this artifact: Add one mana of this artifact's last noted type."));
    }
}
