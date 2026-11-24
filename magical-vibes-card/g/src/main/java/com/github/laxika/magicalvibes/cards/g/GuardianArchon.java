package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RevealChosenPlayerCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "17")
public class GuardianArchon extends Card {

    public GuardianArchon() {
        addStaticEffect(new ChooseOpponentOnEnterEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RevealChosenPlayerCost(),
                        new GrantProtectionFromPlayerEffect(Subjects.chosenPlayer(), Subjects.target(),
                                Duration.UNTIL_END_OF_TURN)
                ),
                "Reveal the player you chose: You and target permanent you control each gain protection from the chosen player until end of turn. Activate only once.",
                TargetFilters.permanentYouControl()
        ).withMaxActivationsPerGame(1));
    }
}
