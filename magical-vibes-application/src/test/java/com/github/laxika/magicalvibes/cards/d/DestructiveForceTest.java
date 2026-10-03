package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DestructiveForce.class, Mountain.class, Forest.class, RuneclawBear.class, DuskdaleWurm.class})
class DestructiveForceTest extends BaseCardTest {

    @Test
    @DisplayName("Both players with 5 or fewer lands lose all their lands")
    void bothPlayersLoseAllLandsWhenFiveOrFewer() {
        // Player1 has 3 lands, Player2 has 2 lands
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // All lands should be sacrificed (both players had ≤5)
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Player with more than 5 lands is prompted to choose which 5 to sacrifice")
    void playerWithMoreThanFiveLandsPromptsChoice() {
        // Player1 has 7 lands
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        // Player2 has 2 lands (will be auto-marked for sacrifice)
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 (active player, APNAP first) must choose 5 of 7 lands.
        // Player2's lands are deferred — all sacrifices happen simultaneously per ruling.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(5);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        // Player2's lands are still on the battlefield (deferred for simultaneous sacrifice)
        assertThat(countPermanents(player2, "Forest")).isEqualTo(2);

        // Player1 chooses 5 lands
        List<Permanent> p1Lands = findPermanents(player1, "Mountain");
        List<UUID> chosen = p1Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, chosen);

        // After choice, ALL lands are sacrificed simultaneously
        // Player1 should have exactly 2 lands remaining
        long p1Remaining = countPermanents(player1, "Mountain");
        assertThat(p1Remaining).isEqualTo(2);

        // Player2's lands should now be gone too
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Both players with more than 5 lands are prompted sequentially, sacrificed simultaneously")
    void bothPlayersWithMoreThanFiveLandsPromptedSequentially() {
        // Player1 has 7 lands, Player2 has 6 lands
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // First player in APNAP order is prompted (active player = player1)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        // First player chooses 5 lands — no sacrifice happens yet (deferred)
        List<Permanent> p1Lands = findPermanents(player1, "Mountain");
        List<UUID> firstChosen = p1Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, firstChosen);

        // All 7 of player1's lands are still on the battlefield (sacrifice is deferred)
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(7);

        // Second player should now be prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        // Second player chooses 5 lands — now ALL chosen lands are sacrificed simultaneously
        List<Permanent> p2Lands = findPermanents(player2, "Forest");
        List<UUID> secondChosen = p2Lands.stream().limit(5).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, secondChosen);

        // Both players should have remaining lands after simultaneous sacrifice
        long p1Remaining = countPermanents(player1, "Mountain");
        long p2Remaining = countPermanents(player2, "Forest");
        assertThat(p1Remaining).isEqualTo(2);
        assertThat(p2Remaining).isEqualTo(1);
    }

    @Test
    @DisplayName("Player with no lands is unaffected by the sacrifice part")
    void playerWithNoLandsIsUnaffected() {
        harness.addToBattlefield(player1, new Mountain());
        // Player2 has no lands, only a creature
        harness.addToBattlefield(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1's land is sacrificed
        harness.assertNotOnBattlefield(player1, "Mountain");

        // Player2's creature is killed by the 5 damage, but no lands to sacrifice
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Deals 5 damage to each creature, killing those with toughness 5 or less")
    void killsCreaturesWithToughnessFiveOrLess() {
        harness.addToBattlefield(player1, new RuneclawBear()); // 2/2
        harness.addToBattlefield(player2, new RuneclawBear()); // 2/2

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Creatures with toughness greater than 5 survive the damage")
    void creaturesWithHighToughnessSurvive() {
        harness.addToBattlefield(player2, new DuskdaleWurm()); // 7/7

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Duskdale Wurm (7/7) survives with 5 damage marked
        harness.assertOnBattlefield(player2, "Duskdale Wurm");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Non-land permanents (creatures) are not sacrificed, only damaged")
    void nonLandPermanentsAreNotSacrificedOnlyDamaged() {
        harness.addToBattlefield(player1, new DuskdaleWurm()); // 7/7
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Mountains are sacrificed (both, since <5)
        harness.assertNotOnBattlefield(player1, "Mountain");

        // Duskdale Wurm survives — not sacrificed and survives 5 damage (7 toughness)
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
    }

    @Test
    @DisplayName("Damage waits for both land choices and resumes after the sacrifices")
    void damageResumesAfterBothPlayersChooseLands() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player2, new Forest());
        }
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new DuskdaleWurm());
        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(findPermanent(player2, "Duskdale Wurm").getMarkedDamage()).isZero();
        harness.handleMultiplePermanentsChosen(player1, findPermanents(player1, "Mountain").stream()
                .limit(5).map(Permanent::getId).toList());

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(findPermanent(player2, "Duskdale Wurm").getMarkedDamage()).isZero();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(6);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(6);
        harness.handleMultiplePermanentsChosen(player2, findPermanents(player2, "Forest").stream()
                .limit(5).map(Permanent::getId).toList());

        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Duskdale Wurm");
        assertThat(findPermanent(player2, "Duskdale Wurm").getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Destructive Force");
    }

    @Test
    @DisplayName("Exactly five lands are all sacrificed")
    void exactlyFiveLandsAreSacrificed() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player2, new Forest());
        }
        harness.setHand(player1, List.of(new DestructiveForce()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Mountain)).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card instanceof Forest)).hasSize(5);
    }
}
