package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "252")
public class ChronatogTotem extends Card {

    public ChronatogTotem() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new AnimatePermanentsEffect(
                        1, 2, List.of(CardSubtype.ATOG), Set.of(), CardColor.BLUE)),
                "{1}{U}: This artifact becomes a 1/2 blue Atog artifact creature until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new BoostSelfEffect(3, 3), new SkipNextEffect(SkipKind.TURN)),
                "{0}: This creature gets +3/+3 until end of turn. You skip your next turn. "
                        + "Activate only once each turn.",
                1
        ).withActivationCondition(new SourceIsCreature(), "Chronatog Totem is not a creature"));
    }
}
