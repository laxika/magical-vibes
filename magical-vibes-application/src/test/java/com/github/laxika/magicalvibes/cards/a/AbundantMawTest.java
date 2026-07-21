package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbundantMawTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, target opponent loses 3 life and controller gains 3")
    void hardcastDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        // Choose opponent for the ON_SELF_CAST trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Abundant Maw"));
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        // Emerge {6}{B} reduced by 2 → {4}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Abundant Maw"))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new AbundantMaw()));
        // Need {4}{B} after reduction; only {3}{B} available
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger cannot target the controller")
    void castTriggerCannotTargetSelf() {
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves even if the creature spell is still on the stack")
    void castTriggerResolvesBeforeCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AbundantMaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        // Trigger sits above the creature spell
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Abundant Maw"));
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Abundant Maw");
    }
}
