package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WretchedGryffTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, controller draws a card")
    void hardcastDrawsACard() {
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve cast trigger
        harness.assertInHand(player1, "Grizzly Bears");

        harness.passBothPriorities(); // resolve creature spell
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wretched Gryff"));
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value, draw")
    void emergeSacrificesReducesCostAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // Emerge {5}{U} reduced by 2 → {3}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wretched Gryff"))
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

        harness.setHand(player1, List.of(new WretchedGryff()));
        // Need {3}{U} after reduction; only {2}{U} available
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
