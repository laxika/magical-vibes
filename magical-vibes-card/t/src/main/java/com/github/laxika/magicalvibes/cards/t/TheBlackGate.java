package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "47")
@CardRegistration(set = "HOC", collectorNumber = "87")
@CardRegistration(set = "LTC", collectorNumber = "80")
@CardRegistration(set = "LTC", collectorNumber = "160")
@CardRegistration(set = "LTC", collectorNumber = "490")
public class TheBlackGate extends Card {

    public TheBlackGate() {
        addStaticEffect(new MayPayLifeOrEntersTappedEffect(3));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new ChoosePlayerEffect(Subjects.matching(Subjects.eachPlayer(), new PlayerHasMostLifePredicate()),
                                "a player with the most life or tied for most life"),
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(new PermanentNotPredicate(
                                new PermanentControlledByPlayersPredicate(Subjects.playersThisWay(StepKind.CHOOSE))),
                                "creatures not controlled by the chosen player")),
                "{1}{B}, {T}: Choose a player with the most life or tied for most life. Target creature can't be blocked by creatures that player controls this turn.",
                TargetFilters.creature()));
    }
}
