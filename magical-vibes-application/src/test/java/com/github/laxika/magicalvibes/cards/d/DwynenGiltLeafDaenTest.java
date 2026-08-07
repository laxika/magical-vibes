package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwynenGiltLeafDaenTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elf creatures you control get +1/+1")
    void buffsOtherOwnElves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new DwynenGiltLeafDaen());

        Permanent elf = findPermanent(player1, "Llanowar Elves");

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dwynen does not buff itself, non-Elves, or opponent Elves")
    void doesNotBuffOthers() {
        harness.addToBattlefield(player1, new DwynenGiltLeafDaen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent dwynen = findPermanent(player1, "Dwynen, Gilt-Leaf Daen");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentElf = findPermanent(player2, "Llanowar Elves");

        assertThat(gqs.getEffectivePower(gd, dwynen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwynen)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Dwynen alone gains 1 life (Dwynen is an attacking Elf)")
    void attackAloneGainsOneLife() {
        addCreatureReady(player1, new DwynenGiltLeafDaen());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life for each attacking Elf, ignoring non-attacking and non-Elf creatures")
    void gainsLifePerAttackingElf() {
        addCreatureReady(player1, new DwynenGiltLeafDaen());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        // Attack with Dwynen, one Llanowar Elves and the Bears; one Elf stays home.
        declareAttackers(player1, List.of(0, 1, 3));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Opponent's attacking Elves are not counted")
    void doesNotCountOpponentElves() {
        addCreatureReady(player1, new DwynenGiltLeafDaen());
        addCreatureReady(player2, new LlanowarElves());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("No trigger when Dwynen does not attack")
    void noTriggerWhenDwynenStaysHome() {
        addCreatureReady(player1, new DwynenGiltLeafDaen());
        addCreatureReady(player1, new LlanowarElves());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }
}
