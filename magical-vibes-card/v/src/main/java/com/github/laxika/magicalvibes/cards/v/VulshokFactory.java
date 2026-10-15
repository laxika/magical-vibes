package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "16")
@CardRegistration(set = "ONC", collectorNumber = "54")
public class VulshokFactory extends Card {

    public VulshokFactory() {
        // {T}: Add {R}. Put a charge counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.RED),
                        new PutCountersOnSelfEffect(CounterType.CHARGE)
                ),
                "{T}: Add {R}. Put a charge counter on this artifact."
        ));

        // {2}{R}, {T}, Sacrifice this artifact: Create an X/X colorless Golem artifact creature token
        // with haste, where X is the number of charge counters on Vulshok Factory. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        SacrificeSelfCost.recordingPermanentSnapshot(),
                        new CreateTokenEffect(
                                "Golem",
                                new CountersOnSource(CounterType.CHARGE),
                                new CountersOnSource(CounterType.CHARGE),
                                null,
                                List.of(CardSubtype.GOLEM),
                                Set.of(Keyword.HASTE),
                                Set.of(CardType.ARTIFACT))
                ),
                "{2}{R}, {T}, Sacrifice this artifact: Create an X/X colorless Golem artifact creature token "
                        + "with haste, where X is the number of charge counters on Vulshok Factory. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
