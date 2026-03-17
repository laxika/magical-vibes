package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraveyardShovelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card from target player's graveyard gains controller 2 life")
    void exileCreatureGainsLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Target player chooses which card to exile — choose the creature (index 0)
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName()).isEqualTo("Shock");
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Exiling a non-creature card does not gain life")
    void exileNonCreatureNoLifeGain() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Choose the non-creature (Shock at index 1)
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Auto-exiles when graveyard has only one card — creature gains life")
    void autoExileSingleCreatureGainsLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only one card — auto-exiled, no choice needed
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Auto-exiles when graveyard has only one card — non-creature no life")
    void autoExileSingleNonCreatureNoLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing when target player's graveyard is empty")
    void emptyGraveyardDoesNothing() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target own graveyard")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Auto-exile the single creature
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Requires tap — cannot activate twice in same turn")
    void requiresTap() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 0);

        // Shovel is now tapped — second activation should fail
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, player2.getId()));
    }
}
