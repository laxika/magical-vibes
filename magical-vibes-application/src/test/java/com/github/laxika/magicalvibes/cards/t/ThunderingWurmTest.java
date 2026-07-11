package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderingWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Thundering Wurm on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        harness.setHand(player1, List.of(new ThunderingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thundering Wurm"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    // ===== ETB with a land card in hand =====

    @Test
    @DisplayName("Discarding a land card keeps Thundering Wurm on the battlefield")
    void discardingLandKeepsWurm() {
        castWurmWithLandInHand();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0); // discard the Forest

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thundering Wurm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the may ability sacrifices Thundering Wurm")
    void decliningSacrificesWurm() {
        castWurmWithLandInHand();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thundering Wurm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thundering Wurm"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== ETB with only non-land cards — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices when controller has no land cards in hand")
    void autoSacrificesWithNoLandInHand() {
        harness.setHand(player1, List.of(new ThunderingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thundering Wurm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thundering Wurm"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Filtered discard — only land cards are valid =====

    @Test
    @DisplayName("Discard choice only shows land card indices when hand has mixed types")
    void discardChoiceOnlyShowsLandIndices() {
        harness.setHand(player1, List.of(new ThunderingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        // Hand: [GrizzlyBears, Forest, GrizzlyBears, Forest]
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → may ability

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactlyInAnyOrder(1, 3);
    }

    // ===== Helpers =====

    private void castWurmWithLandInHand() {
        harness.setHand(player1, List.of(new ThunderingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new Forest()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
