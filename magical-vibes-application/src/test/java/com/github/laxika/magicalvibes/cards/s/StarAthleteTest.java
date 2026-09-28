package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarAthlete.class, GrizzlyBears.class})
class StarAthleteTest extends BaseCardTest {

    @Test
    void controllerMaySacrificeTheTarget() {
        addCreatureReady(player1, new StarAthlete());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    void controllerMayDeclineAndTakeDamage() {
        addCreatureReady(player1, new StarAthlete());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 15);
    }

    @Test
    void blitzGrantsHasteDrawsOnDeathAndSacrificesAtTheNextEndStep() {
        harness.setHand(player1, List.of(new StarAthlete()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent starAthlete = findPermanent(player1, "Star Athlete");
        assertThat(gqs.hasKeyword(gd, starAthlete, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Star Athlete");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
