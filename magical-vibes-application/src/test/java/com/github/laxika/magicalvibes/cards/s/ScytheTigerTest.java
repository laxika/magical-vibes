package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScytheTigerTest extends BaseCardTest {

    private void castScytheTiger() {
        harness.setHand(player1, List.of(new ScytheTiger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        castScytheTiger();

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the land sacrifice keeps Scythe Tiger")
    void sacrificingLandKeepsScytheTiger() {
        harness.addToBattlefield(player1, new Mountain());
        castScytheTiger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Mountain".equals(permanent.getCard().getName()))
                .findFirst().orElseThrow().getId());

        harness.assertOnBattlefield(player1, "Scythe Tiger");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining the land sacrifice sacrifices Scythe Tiger")
    void decliningSacrificesScytheTiger() {
        harness.addToBattlefield(player1, new Mountain());
        castScytheTiger();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Opponent-controlled lands do not satisfy the ability")
    void opponentLandDoesNotCount() {
        harness.addToBattlefield(player2, new Mountain());
        castScytheTiger();

        harness.assertNotOnBattlefield(player1, "Scythe Tiger");
        harness.assertInGraveyard(player1, "Scythe Tiger");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
