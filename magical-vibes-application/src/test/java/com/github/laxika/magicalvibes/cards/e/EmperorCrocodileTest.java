package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmperorCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Cast with no other creatures — state trigger fires and Crocodile is sacrificed")
    void sacrificedWhenControllingNoOtherCreatures() {
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // State trigger is on the stack — Crocodile still alive
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Emperor Crocodile"));

        // Resolve state trigger → Crocodile is sacrificed
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Emperor Crocodile"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Emperor Crocodile"));
    }

    @Test
    @DisplayName("Survives while controlling another creature — no state trigger")
    void survivesWithAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Emperor Crocodile"));
    }

    @Test
    @DisplayName("Sacrificed when the last other creature dies")
    void sacrificedWhenLastOtherCreatureDies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Both creatures present, no trigger yet
        assertThat(gd.stack).isEmpty();

        // Kill the Bears with Shock (2 damage)
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsId);

        // Bears gone → state trigger fires; resolve it → Crocodile sacrificed
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Emperor Crocodile"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Emperor Crocodile"));
    }

    @Test
    @DisplayName("Controlling another creature owned via opponent does not count — sacrificed")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // state trigger fires — opponent's creature doesn't count
        harness.passBothPriorities(); // resolve → sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Emperor Crocodile"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Emperor Crocodile"));
    }
}
