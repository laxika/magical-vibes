package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GutwrencherOni;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodSpeaker.class, BlindWithAnger.class, GutwrencherOni.class, HumbleBudoka.class})
class BloodSpeakerTest extends BaseCardTest {

    private void prepareMain(com.github.laxika.magicalvibes.model.Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting the upkeep trigger sacrifices Blood Speaker and tutors a Demon to hand")
    void upkeepSacrificeTutorsDemon() {
        harness.addToBattlefield(player1, new BloodSpeaker());
        harness.setLibrary(player1, List.of(new GutwrencherOni(), new HumbleBudoka()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Blood Speaker");
        harness.assertInGraveyard(player1, "Blood Speaker");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName)).containsExactly("Gutwrencher Oni");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Gutwrencher Oni");
    }

    @Test
    @DisplayName("Declining the upkeep trigger keeps Blood Speaker and searches nothing")
    void upkeepDeclineKeepsCreature() {
        harness.addToBattlefield(player1, new BloodSpeaker());
        harness.setLibrary(player1, List.of(new GutwrencherOni()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Blood Speaker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Demon entering under your control returns Blood Speaker from the graveyard to hand")
    void demonEntersReturnsFromGraveyard() {
        harness.setGraveyard(player1, List.of(new BloodSpeaker()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new GutwrencherOni()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {3}{B}{B}
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Blood Speaker");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> "Blood Speaker".equals(c.getName()));
    }

    @Test
    @DisplayName("A non-Demon creature entering does not return Blood Speaker")
    void nonDemonDoesNotReturn() {
        harness.setGraveyard(player1, List.of(new BloodSpeaker()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blood Speaker");
    }

    @Test
    @DisplayName("A Demon an opponent controls entering does not return Blood Speaker")
    void opponentDemonDoesNotReturn() {
        harness.setGraveyard(player1, List.of(new BloodSpeaker()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new GutwrencherOni()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blood Speaker");
    }

    @Test
    @DisplayName("Sacrificing without a Demon in the library leaves Blood Speaker in the graveyard")
    void upkeepSacrificeWithoutDemonFindsNothing() {
        harness.addToBattlefield(player1, new BloodSpeaker());
        harness.setLibrary(player1, List.of(new HumbleBudoka()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Blood Speaker");
        harness.assertInGraveyard(player1, "Blood Speaker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("If an opponent gains control before the upkeep choice resolves, Blood Speaker cannot search")
    void upkeepSearchRequiresSuccessfulSacrifice() {
        harness.addToBattlefield(player1, new BloodSpeaker());
        harness.setLibrary(player1, List.of(new GutwrencherOni()));
        advanceToUpkeep(player1);
        UUID bloodSpeakerId = harness.getPermanentId(player1, "Blood Speaker");
        var bloodSpeaker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bloodSpeakerId)).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(bloodSpeaker);
        gd.playerBattlefields.get(player2.getId()).add(bloodSpeaker);
        harness.passBothPriorities(); // resolve the upkeep trigger -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Blood Speaker");
    }
}
