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
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Star Athlete");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(StarAthlete.class)
    void blitzHasHasteImmediatelyWhenTheSpellResolves() {
        harness.setHand(player1, List.of(new StarAthlete()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent athlete = findPermanent(player1, "Star Athlete");
        assertThat(gqs.hasKeyword(gd, athlete, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(StarAthlete.class)
    void normalCastingDoesNotGrantHasteOrScheduleSacrifice() {
        harness.setHand(player1, List.of(new StarAthlete()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent athlete = findPermanent(player1, "Star Athlete");
        assertThat(gqs.hasKeyword(gd, athlete, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Star Athlete");
        harness.assertNotInGraveyard(player1, "Star Athlete");
    }

    @Test
    @CardUsed(StarAthlete.class)
    void canTargetYourOwnNonlandPermanentAndDeclineSacrifice() {
        addCreatureReady(player1, new StarAthlete());
        Permanent target = addCreatureReady(player1, new StarAthlete());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 15);
        assertThat(findPermanents(player1, "Star Athlete")).hasSize(2);
    }

    @Test
    @CardUsed(StarAthlete.class)
    void blitzDrawsWhenSacrificedBeforeTheEndStep() {
        harness.setHand(player1, List.of(new StarAthlete()));
        harness.setLibrary(player1, List.of(new StarAthlete()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent athlete = findPermanent(player1, "Star Athlete");
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, athlete.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Star Athlete");
        harness.assertInHand(player1, "Star Athlete");
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(StarAthlete.class)
    void normallyCastCreatureDoesNotDrawWhenItDies() {
        harness.setHand(player1, List.of(new StarAthlete()));
        harness.setLibrary(player1, List.of(new StarAthlete()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent athlete = findPermanent(player1, "Star Athlete");
        athlete.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, athlete.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Star Athlete");
        harness.assertNotInHand(player1, "Star Athlete");
        harness.assertLife(player1, 20);
    }
}
