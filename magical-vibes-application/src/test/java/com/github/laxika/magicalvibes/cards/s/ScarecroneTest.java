package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarecroneTest extends BaseCardTest {

    // ===== {1}, Sacrifice a Scarecrow: Draw a card. =====

    @Test
    @DisplayName("Sacrificing a Scarecrow (itself) draws a card")
    void sacrificeScarecrowDrawsCard() {
        addReadyScarecrone(player1);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Scarecrone is the only Scarecrow → auto-sacrifices itself.
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarecrone"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    // ===== {4}, {T}: Return target artifact creature card from your graveyard. =====

    @Test
    @DisplayName("Returns an artifact creature card from graveyard to battlefield")
    void returnsArtifactCreatureFromGraveyard() {
        addReadyScarecrone(player1);
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Cannot return a non-artifact creature card")
    void cannotReturnNonArtifactCreature() {
        addReadyScarecrone(player1);
        // Grizzly Bears is a creature but not an artifact; Ornithopter is the valid choice.
        harness.setGraveyard(player1, List.of(new Ornithopter(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Index 1 is Grizzly Bears (not an artifact) — not a legal choice.
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Reanimation ability requires tapping and cannot be activated when tapped")
    void reanimationRequiresUntapped() {
        Permanent scarecrone = addReadyScarecrone(player1);
        scarecrone.tap();
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helpers =====

    private Permanent addReadyScarecrone(Player player) {
        Permanent scarecrone = new Permanent(new Scarecrone());
        scarecrone.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(scarecrone);
        return scarecrone;
    }
}
