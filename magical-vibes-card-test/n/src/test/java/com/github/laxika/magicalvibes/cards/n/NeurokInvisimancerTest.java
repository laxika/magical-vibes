package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokInvisimancer.class, MoriokReaver.class})
class NeurokInvisimancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unblockable this turn")
    void etbMakesTargetCreatureUnblockable() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Neurok Invisimancer");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent target = findPermanent(player2, "Moriok Reaver");
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Can target own creature with ETB")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent target = findPermanent(player1, "Moriok Reaver");
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting onto an empty battlefield still triggers and can target itself")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Neurok Invisimancer");
        UUID selfId = harness.getPermanentId(player1, "Neurok Invisimancer");
        harness.handlePermanentChosen(player1, selfId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(selfId);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Neurok Invisimancer").isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Neurok Invisimancer cannot be blocked without its ETB effect")
    void innateRestrictionPreventsBlocking() {
        Permanent invisimancer = addCreatureReady(player1, new NeurokInvisimancer());
        addCreatureReady(player2, new MoriokReaver());

        assertThat(gqs.hasCantBeBlocked(gd, invisimancer)).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB restriction expires in cleanup while the innate restriction remains")
    void temporaryRestrictionExpiresInCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        Permanent invisimancer = findPermanent(player1, "Neurok Invisimancer");
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, invisimancer)).isTrue();
    }

    @Test
    @DisplayName("ETB resolves independently after Neurok Invisimancer leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent invisimancer = findPermanent(player1, "Neurok Invisimancer");
        gd.playerBattlefields.get(player1.getId()).remove(invisimancer);
        gd.playerGraveyards.get(player1.getId()).add(invisimancer.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
