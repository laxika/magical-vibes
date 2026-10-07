package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stranglehold.class, DiabolicTutor.class, Forest.class, GrizzlyBears.class, CaptureOfJingzhou.class, SongOfTheDryads.class})
class StrangleholdTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot search libraries")
    void opponentsCannotSearchLibraries() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("prevented by Stranglehold"));
    }

    @Test
    @DisplayName("The controller can search a library")
    void controllerCanSearchLibrary() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Skips an opponent's extra turn")
    void skipsOpponentExtraTurn() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.setHand(player2, List.of(new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player2.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Does not skip the controller's extra turn")
    void doesNotSkipControllerExtraTurn() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("Opponents can search after Song of the Dryads removes Stranglehold's abilities")
    void opponentsCanSearchAfterAbilitiesAreRemoved() {
        Permanent stranglehold = harness.addToBattlefieldAndReturn(player1, new Stranglehold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SongOfTheDryads(), new DiabolicTutor()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castEnchantment(player2, 0, stranglehold.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, stranglehold)).isTrue();

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("An opponent takes an extra turn after Song of the Dryads removes Stranglehold's abilities")
    void opponentTakesExtraTurnAfterAbilitiesAreRemoved() {
        Permanent stranglehold = harness.addToBattlefieldAndReturn(player1, new Stranglehold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SongOfTheDryads(), new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castEnchantment(player2, 0, stranglehold.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, stranglehold)).isTrue();
        harness.castAndResolveSorcery(player2, 0, 0);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("All consecutive opponent extra turns are skipped")
    void skipsConsecutiveOpponentExtraTurns() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CaptureOfJingzhou(), new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 10);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player2.getId(), player2.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isFalse();
        assertThat(gd.extraTurns).isEmpty();
    }
}
