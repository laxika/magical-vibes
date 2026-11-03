package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "12")
public class HeraldOfAnafenza extends Card {

    public HeraldOfAnafenza() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {2}{W} ({2}{W}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withOutlast());

        addTrigger(Triggers.youActivateAbility()
                        .withKind(AbilityTrigger.AbilityKind.OUTLAST)
                        .withSource(new PermanentIsSourceCardPredicate()),
                new CreateTokenEffect("Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
