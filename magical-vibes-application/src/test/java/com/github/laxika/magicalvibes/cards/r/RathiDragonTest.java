package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RathiDragonTest extends BaseCardTest {

    private long mountainsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .count();
    }

    private void castRathiDragon() {
        harness.setHand(player1, List.of(new RathiDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rathi Dragon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rathi Dragon"));
        // The lone Mountain is untouched.
        assertThat(mountainsControlledBy(player1.getId())).isEqualTo(1);
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
        assertThat(mountainsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rathi Dragon"));
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
        assertThat(mountainsControlledBy(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rathi Dragon"));
    }

    @Test
    @DisplayName("Declining sacrifices Rathi Dragon and keeps the Mountains")
    void declineSacrificesDragon() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        castRathiDragon();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rathi Dragon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rathi Dragon"));
        assertThat(mountainsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Mountains don't satisfy the requirement")
    void opponentMountainsDontCount() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        castRathiDragon();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rathi Dragon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rathi Dragon"));
        assertThat(mountainsControlledBy(player2.getId())).isEqualTo(2);
    }
}
