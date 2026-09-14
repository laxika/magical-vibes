package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicBreak.class, Forest.class, FreshVolunteers.class, Mountain.class})
class TectonicBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices X lands of their choice")
    void eachPlayerSacrificesXLandsOfTheirChoice() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new FreshVolunteers());
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
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.maxCount()).isEqualTo(2);
        assertThat(((MultiPermanentChoiceContext.ForcedSacrifice) secondChoice.context()).accumulatedSacrificeIds())
                .hasSize(2);
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(3);

        List<UUID> player2Choices = findPermanents(player2, "Forest").stream()
                .limit(2).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, player2Choices);

        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Waits to sacrifice a player's remaining lands until all choices are made")
    void sacrificesFewerThanXLandsSimultaneouslyWithChosenLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new TectonicBreak()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(3);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);

        List<UUID> player1Choices = findPermanents(player1, "Mountain").stream()
                .limit(2).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, player1Choices);

        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrifices all available lands when a player controls fewer than X")
    void sacrificesAllAvailableLandsBelowX() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new FreshVolunteers());

        harness.setHand(player1, List.of(new TectonicBreak()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("X of zero leaves every land on the battlefield")
    void zeroXLandsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new TectonicBreak()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
