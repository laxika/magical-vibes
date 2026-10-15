package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XathridDemon.class, RuneclawBear.class, GiantSpider.class, GiantGrowth.class,
        Cloudshift.class, AlexiosDeimosOfKosmos.class})
class XathridDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and controller loses 7 life when no other creatures are present")
    void tapAndLose7LifeWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("Xathrid Demon is tapped when no other creatures")
    void demonIsTappedWhenNoOtherCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        Permanent demon = findPermanent(player1, "Xathrid Demon");
        assertThat(demon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent does not lose life when controller can't sacrifice")
    void opponentUnaffectedWhenNoSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Auto-sacrifices the only other creature")
    void autoSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear()); // 2/2

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Opponent loses life equal to sacrificed creature's power (auto-sacrifice)")
    void opponentLosesLifeEqualToPowerAutoSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear()); // 2/2
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Controller does not lose life when sacrifice succeeds")
    void controllerDoesNotLoseLifeOnSuccessfulSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Xathrid Demon is not tapped when sacrifice succeeds")
    void demonNotTappedOnSuccessfulSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        Permanent demon = findPermanent(player1, "Xathrid Demon");
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Xathrid Demon remains on the battlefield after sacrificing another creature")
    void demonRemainsAfterSacrifice() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertOnBattlefield(player1, "Xathrid Demon");
    }

    @Test
    @DisplayName("Opponent loses life equal to bigger creature's power")
    void opponentLosesLifeEqualToBiggerCreaturePower() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new GiantSpider()); // 2/4
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Prompts player to choose when multiple other creatures are present")
    void promptsChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(bears.getId(), spider.getId());
    }

    @Test
    @DisplayName("Xathrid Demon itself is not in the valid sacrifice choices")
    void demonNotInValidChoices() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new GiantSpider());

        Permanent demonPerm = findPermanent(player1, "Xathrid Demon");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(demonPerm.getId());
    }

    @Test
    @DisplayName("Player chooses creature to sacrifice, opponent loses life equal to its power")
    void playerChoosesCreatureOpponentLosesLife() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new RuneclawBear()); // 2/2
        addCreatureReady(player1, new GiantSpider()); // 2/4
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        // Opponent loses life equal to Runeclaw Bear' power (2)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new XathridDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Opponent's creatures are not valid sacrifice targets")
    void opponentCreaturesNotValidTargets() {
        harness.addToBattlefield(player1, new XathridDemon());
        addCreatureReady(player2, new RuneclawBear());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Controller loses 7 life and demon is tapped (no OTHER creatures controller owns)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
        // Opponent's creature is untouched, opponent doesn't lose life from sacrifice
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Life loss at low life can kill controller (game ends)")
    void lifeLossAtLowLifeEndsGame() {
        harness.addToBattlefield(player1, new XathridDemon());
        harness.setLife(player1, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3 - 7);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life loss is not damage — cannot be prevented by color-based damage prevention")
    void lifeLossNotPreventedByColorPrevention() {
        harness.addToBattlefield(player1, new XathridDemon());
        gd.preventDamageFromColors.add(com.github.laxika.magicalvibes.model.CardColor.BLACK);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Life loss is NOT damage, so it is NOT prevented
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    void sacrificeUsesPowerIncludingTemporaryBoost() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        advanceToUpkeep(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void chosenSacrificeUsesPowerIncludingTemporaryBoost() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new GiantSpider());
        advanceToUpkeep(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).contains(player1.getId());
    }

    @Test
    void returnedDemonCanBeSacrificedToItsOriginalTrigger() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new XathridDemon());
        advanceToUpkeep(player1);

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, demon.getId());
        harness.passBothPriorities();
        Permanent returnedDemon = findPermanent(player1, "Xathrid Demon");
        assertThat(returnedDemon.getId()).isNotEqualTo(demon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Xathrid Demon");
        harness.assertInGraveyard(player1, "Xathrid Demon");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void unsacrificableCreatureDoesNotAvoidPenalty() {
        harness.addToBattlefield(player1, new XathridDemon());
        advanceToUpkeep(player1);
        // Set up a creature that arrived after the upkeep triggers were collected.
        harness.addToBattlefield(player1, new AlexiosDeimosOfKosmos());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        assertThat(findPermanent(player1, "Xathrid Demon").isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void unsacrificableCreatureIsExcludedFromSacrificeChoices() {
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new AlexiosDeimosOfKosmos());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(bears.getId(), spider.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
