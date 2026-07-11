package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimevalForceTest extends BaseCardTest {

    private long forestsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .count();
    }

    private void castPrimevalForce() {
        harness.setHand(player1, List.of(new PrimevalForce()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than three Forests")
    void autoSacrificesWithoutThreeForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPrimevalForce();

        // The cost can't be paid, so Primeval Force is sacrificed automatically.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Primeval Force"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Primeval Force"));
        // The Forests are untouched.
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Prompts a may ability when controller has three or more Forests")
    void promptsMayAbilityWithThreeForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPrimevalForce();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly three Forests sacrifices all three and keeps Primeval Force")
    void acceptWithExactlyThreeForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPrimevalForce();

        harness.handleMayAbilityChosen(player1, true);

        // All three Forests sacrificed without a further choice; Primeval Force stays.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Primeval Force"));
    }

    @Test
    @DisplayName("Accepting with four Forests lets controller choose which three to sacrifice")
    void acceptWithFourForestsChoosesThree() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPrimevalForce();

        harness.handleMayAbilityChosen(player1, true);

        // More Forests than needed — a multi-permanent choice is required.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> forestIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .map(p -> p.getId())
                .limit(3)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, forestIds);

        // Three Forests sacrificed, one remains; Primeval Force stays.
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Primeval Force"));
    }

    @Test
    @DisplayName("Declining sacrifices Primeval Force and keeps the Forests")
    void declineSacrificesForce() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPrimevalForce();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Primeval Force"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Primeval Force"));
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's Forests don't satisfy the requirement")
    void opponentForestsDontCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castPrimevalForce();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Primeval Force"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Primeval Force"));
        assertThat(forestsControlledBy(player2.getId())).isEqualTo(3);
    }
}
