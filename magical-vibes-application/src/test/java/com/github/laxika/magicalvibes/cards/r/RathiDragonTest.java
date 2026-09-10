package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RathiDragon.class, Mountain.class, Forest.class})
class RathiDragonTest extends BaseCardTest {

    private void castRathiDragon() {
        harness.castFromHand(player1, new RathiDragon(), "{2}{R}{R}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than two Mountains")
    void autoSacrificesWithoutTwoMountains() {
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        // No choice — the cost can't be paid, so Rathi Dragon is sacrificed automatically.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Rathi Dragon");
        harness.assertInGraveyard(player1, "Rathi Dragon");
        // The lone Mountain is untouched.
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts a may ability when controller has two or more Mountains")
    void promptsMayAbilityWithTwoMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly two Mountains sacrifices both and keeps Rathi Dragon")
    void acceptWithExactlyTwoMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        harness.handleMayAbilityChosen(player1, true);

        // Both Mountains sacrificed without a further choice; Rathi Dragon stays.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Rathi Dragon");
    }

    @Test
    @DisplayName("Accepting sacrifices Mountains but not other land types")
    void acceptSacrificesOnlyMountains() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(0);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rathi Dragon");
    }

    @Test
    @DisplayName("Accepting with three Mountains lets controller choose which two to sacrifice")
    void acceptWithThreeMountainsChoosesTwo() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        harness.handleMayAbilityChosen(player1, true);

        // More Mountains than needed — a multi-permanent choice is required.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> mountainIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .map(p -> p.getId())
                .limit(2)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, mountainIds);

        // Two Mountains sacrificed, one remains; Rathi Dragon stays.
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rathi Dragon");
    }

    @Test
    @DisplayName("Declining sacrifices Rathi Dragon and keeps the Mountains")
    void declineSacrificesDragon() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rathi Dragon");
        harness.assertInGraveyard(player1, "Rathi Dragon");
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Mountains don't satisfy the requirement")
    void opponentMountainsDontCount() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        castRathiDragon();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Rathi Dragon");
        harness.assertInGraveyard(player1, "Rathi Dragon");
        assertThat(countPermanents(player2, "Mountain")).isEqualTo(2);
    }
}
