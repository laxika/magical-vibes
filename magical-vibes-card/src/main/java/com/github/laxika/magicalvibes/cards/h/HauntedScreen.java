package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "250")
public class HauntedScreen extends Card {

    public HauntedScreen() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED))
                ),
                "{T}, Pay 1 life: Add {G}, {U}, or {R}."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 7),
                        new AnimatePermanentsEffect(
                                0, 0, List.of(CardSubtype.SPIRIT), Set.of(), null,
                                Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.PERMANENT)
                ),
                "{7}: Put seven +1/+1 counters on this artifact. It becomes a 0/0 Spirit creature "
                        + "in addition to its other types. Activate only once."
        ).withMaxActivationsPerGame(1));
    }
}
