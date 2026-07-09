package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WhirlpoolWhelmTest extends BaseCardTest {

    private UUID prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player2, new GrizzlyBears()); // the bounce target
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}

        return harness.getPermanentId(player2, "Grizzly Bears");
    }

    // Caster (player1) wins the clash: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash and accepting puts the creature on top of its owner's library")
    void wonClashAcceptPutsOnTopOfLibrary() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Won clash → controller is offered the "put on top instead" choice.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Winning the clash and declining returns the creature to its owner's hand")
    void wonClashDeclineReturnsToHand() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isNotEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Losing the clash returns the creature to its owner's hand with no choice offered")
    void lostClashReturnsToHand() {
        UUID targetId = prepare();
        stackClashLossForCaster();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // No "put on top" choice on a loss — the creature simply goes to hand.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID landId = harness.getPermanentId(player2, "Forest");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
