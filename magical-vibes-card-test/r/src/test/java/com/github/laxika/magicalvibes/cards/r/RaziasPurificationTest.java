package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaziasPurification.class, BorosRecruit.class, BorosSignet.class, Forest.class})
class RaziasPurificationTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses three permanents and sacrifices the rest simultaneously")
    void eachPlayerChoosesThreePermanents() {
        Permanent player1Recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Signet = harness.addToBattlefieldAndReturn(player1, new BorosSignet());
        Permanent player1SecondRecruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent player1SecondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2Signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        Permanent player2SecondRecruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        castRaziasPurification();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        assertThat(player1Choice.validIds()).containsExactly(
                player1Recruit.getId(), player1Forest.getId(), player1Signet.getId(),
                player1SecondRecruit.getId(), player1SecondForest.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1Forest.getId(), player1Recruit.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Recruit, player1Forest, player1Signet, player1SecondRecruit, player1SecondForest);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Signet.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Signet, player1SecondRecruit, player1SecondForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(
                player2Recruit, player2Forest, player2SecondRecruit);
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("Players with three or fewer permanents keep them all")
    void threeOrFewerPermanentsAreUntouched() {
        Permanent player1Recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Signet = harness.addToBattlefieldAndReturn(player1, new BorosSignet());
        Permanent player2Signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        castRaziasPurification();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Recruit, player1Forest, player1Signet);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Signet);
    }

    @Test
    @DisplayName("An opponent sacrifices excess permanents even when the caster controls none")
    void onlyOpponentHasExcessPermanents() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        Permanent secondRecruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castRaziasPurification();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(
                recruit.getId(), forest.getId(), signet.getId(), secondRecruit.getId(), secondForest.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(recruit.getId(), secondRecruit.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(forest, signet, secondForest);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Boros Recruit");
        harness.assertInGraveyard(player1, "Razia's Purification");
    }

    @Test
    @DisplayName("Empty battlefields need no choices and the spell finishes resolving")
    void emptyBattlefieldsNeedNoChoices() {
        castRaziasPurification();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Razia's Purification");
    }

    private void castRaziasPurification() {
        harness.castFromHand(player1, new RaziasPurification(), "{4}{R}{W}");
        harness.passBothPriorities();
    }
}
