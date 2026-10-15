package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "203")
public class RootwireAmalgam extends Card {

    public RootwireAmalgam() {
        addPrototype("{1}{G}", CardColor.GREEN, 2, 3);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new Fixed(1),
                                "Golem",
                                new Scaled(new SourcePower(), 3),
                                new Scaled(new SourcePower(), 3),
                                null,
                                null,
                                List.of(CardSubtype.GOLEM),
                                Set.of(),
                                Set.of(CardType.ARTIFACT),
                                false,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of(Keyword.HASTE)
                        )
                ),
                "{3}{G}{G}, Sacrifice Rootwire Amalgam: Create an X/X colorless Golem artifact creature token, where X is three times Rootwire Amalgam's power. It gains haste until end of turn.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
