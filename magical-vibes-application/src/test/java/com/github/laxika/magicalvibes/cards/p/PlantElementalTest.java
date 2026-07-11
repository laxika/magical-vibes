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

class PlantElementalTest extends BaseCardTest {

    private long forestsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .count();
    }

    private void castPlantElemental() {
        harness.setHand(player1, List.of(new PlantElemental()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no Forest")
    void autoSacrificesWithoutForest() {
        castPlantElemental();

        // No choice — the cost can't be paid, so Plant Elemental is sacrificed automatically.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plant Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plant Elemental"));
    }

    @Test
    @DisplayName("Prompts a may ability when controller has a Forest")
    void promptsMayAbilityWithForest() {
        harness.addToBattlefield(player1, new Forest());
        castPlantElemental();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly one Forest sacrifices it and keeps Plant Elemental")
    void acceptWithExactlyOneForest() {
        harness.addToBattlefield(player1, new Forest());
        castPlantElemental();

        harness.handleMayAbilityChosen(player1, true);

        // The lone Forest is sacrificed without a further choice; Plant Elemental stays.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plant Elemental"));
    }

    @Test
    @DisplayName("Accepting with two Forests lets controller choose which one to sacrifice")
    void acceptWithTwoForestsChoosesOne() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPlantElemental();

        harness.handleMayAbilityChosen(player1, true);

        // More Forests than needed — a multi-permanent choice is required.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> forestIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .map(p -> p.getId())
                .limit(1)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, forestIds);

        // One Forest sacrificed, one remains; Plant Elemental stays.
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plant Elemental"));
    }

    @Test
    @DisplayName("Declining sacrifices Plant Elemental and keeps the Forest")
    void declineSacrificesElemental() {
        harness.addToBattlefield(player1, new Forest());
        castPlantElemental();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plant Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plant Elemental"));
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Forest doesn't satisfy the requirement")
    void opponentForestDoesntCount() {
        harness.addToBattlefield(player2, new Forest());
        castPlantElemental();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plant Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plant Elemental"));
        assertThat(forestsControlledBy(player2.getId())).isEqualTo(1);
    }
}
