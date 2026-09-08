package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TectonicBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices up to X lands of their choice")
    void eachPlayerSacrificesUpToXLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new TectonicBreak()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.maxCount()).isEqualTo(2);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());

        List<UUID> player1Choices = findPermanents(player1, "Mountain").stream()
                .limit(2).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, player1Choices);

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(((MultiPermanentChoiceContext.ForcedSacrifice) secondChoice.context()).accumulatedSacrificeIds())
                .hasSize(2);

        List<UUID> player2Choices = findPermanents(player2, "Forest").stream()
                .limit(2).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, player2Choices);

        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices all available lands when a player controls fewer than X")
    void sacrificesAllAvailableLandsBelowX() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TectonicBreak()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
