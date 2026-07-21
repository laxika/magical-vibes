package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulManipulationTest extends BaseCardTest {

    private void giveManaTo(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    // ===== Mode 0: Counter target creature spell =====

    @Test
    @DisplayName("Mode 0 — counters a creature spell")
    void mode0CountersCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new SoulManipulation()));
        giveManaTo(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, elves.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Mode 0 — cannot counter a non-creature spell")
    void mode0CannotCounterNonCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new SoulManipulation()));
        giveManaTo(player2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player2, 0, 1, new int[]{0}, might.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mode 1: Return target creature card from your graveyard =====

    @Test
    @DisplayName("Mode 1 — returns a creature card from your graveyard to your hand")
    void mode1ReturnsCreatureFromGraveyard() {
        Card deadCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(deadCreature));
        harness.setHand(player1, List.of(new SoulManipulation()));
        giveManaTo(player1);

        harness.castModalInstant(player1, 0, 1, List.of());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(deadCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(deadCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Mode 2: both =====

    @Test
    @DisplayName("Mode 2 — counters a creature spell and returns a creature card from graveyard")
    void mode2CountersAndReturns() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card deadBear = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(deadBear));
        harness.setHand(player2, List.of(new SoulManipulation()));
        giveManaTo(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, elves.getId(), List.of());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of(deadBear.getId()));
        harness.passBothPriorities();

        // Counter half: Llanowar Elves countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Return half: Grizzly Bears back in player2's hand
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mode 2 — with no creature in graveyard, still counters the creature spell")
    void mode2CountersWhenGraveyardEmpty() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new SoulManipulation()));
        giveManaTo(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{2}, elves.getId(), List.of());

        // No creature card to return — no graveyard prompt, spell goes straight onto the stack.
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }
}
