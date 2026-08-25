package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaziasPurification.class, Crusade.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Millstone.class})
class RaziasPurificationTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses three permanents and sacrifices the rest simultaneously")
    void eachPlayerChoosesThreePermanents() {
        Permanent player1Crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1HillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent player1Millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent player2Crusade = harness.addToBattlefieldAndReturn(player2, new Crusade());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castRaziasPurification();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        assertThat(player1Choice.validIds()).containsExactly(
                player1Crusade.getId(), player1Forest.getId(), player1Bears.getId(),
                player1HillGiant.getId(), player1Millstone.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1Forest.getId(), player1Crusade.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Crusade, player1Forest, player1Bears, player1HillGiant, player1Millstone);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Millstone.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Bears, player1HillGiant, player1Millstone);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(
                player2Crusade, player2Forest, player2Bears);
        harness.assertInGraveyard(player1, "Crusade");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Players with three or fewer permanents keep them all")
    void threeOrFewerPermanentsAreUntouched() {
        Permanent player1Crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castRaziasPurification();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(
                player1Crusade, player1Forest, player1Bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Millstone);
    }

    private void castRaziasPurification() {
        harness.setHand(player1, List.of(new RaziasPurification()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
