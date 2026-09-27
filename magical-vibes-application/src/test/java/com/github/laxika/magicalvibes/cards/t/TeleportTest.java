package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.h.Hammerheim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Teleport.class, DurkwoodBoars.class, Hammerheim.class})
class TeleportTest extends BaseCardTest {

    @Test
    @DisplayName("During the declare attackers step, Teleport makes a target creature unblockable")
    void makesTargetUnblockableDuringDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID targetId = harness.getPermanentId(player1, "Durkwood Boars");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Durkwood Boars").isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Teleport cannot be cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Durkwood Boars");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Teleport can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Durkwood Boars").isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Teleport cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.forceActivePlayer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Teleport prevents the targeted attacker from being blocked")
    void targetedAttackerCannotBeBlocked() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Teleport's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID targetId = harness.getPermanentId(player1, "Durkwood Boars");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Durkwood Boars").isCantBeBlocked()).isFalse();
    }
}
