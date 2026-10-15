package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwynenGiltLeafDaen.class, LlanowarElves.class, GrizzlyBears.class,
        LeafGilder.class, Disperse.class})
class DwynenGiltLeafDaenTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elf creatures you control get +1/+1")
    void buffsOtherOwnElves() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new DwynenGiltLeafDaen());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dwynen does not buff itself, non-Elves, or opponent Elves")
    void doesNotBuffOthers() {
        Permanent dwynen = harness.addToBattlefieldAndReturn(player1, new DwynenGiltLeafDaen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

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
    @DisplayName("Opponent's Elves are not counted")
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

    @Test
    @DisplayName("The attack trigger survives Dwynen leaving and counts only remaining attacking Elves")
    void countsRemainingElvesAfterDwynenLeaves() {
        Permanent dwynen = addCreatureReady(player1, new DwynenGiltLeafDaen());
        Permanent elf = addCreatureReady(player1, new LeafGilder());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            harness.assertLife(player1, 20);
            harness.castAndResolveInstant(player1, 0, dwynen.getId());
            harness.assertNotOnBattlefield(player1, "Dwynen, Gilt-Leaf Daen");
            assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
            resolveAllTriggers();
        });

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is gained if the only attacking Elf leaves before the trigger resolves")
    void gainsNoLifeWhenNoAttackingElvesRemain() {
        Permanent dwynen = addCreatureReady(player1, new DwynenGiltLeafDaen());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.assertLife(player1, 20);
            harness.castAndResolveInstant(player1, 0, dwynen.getId());
            harness.assertNotOnBattlefield(player1, "Dwynen, Gilt-Leaf Daen");
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
