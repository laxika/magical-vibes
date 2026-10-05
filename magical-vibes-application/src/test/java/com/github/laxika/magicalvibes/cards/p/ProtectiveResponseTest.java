package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProtectiveResponse.class, GrizzlyBears.class, SavannahLions.class})
class ProtectiveResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature")
    void destroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target blocking creature")
    void destroysBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());

        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Convoke can pay the generic portion of the spell")
    void convokePaysGenericMana() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(attacker.getId()), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a creature that is neither attacking nor blocking")
    void rejectsNonCombatCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White convoker pays the white cost even with summoning sickness")
    void convokePaysWhiteManaWithSummoningSickCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        convoker.setSummoningSick(true);
        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(attacker.getId()), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(convoker.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Protective Response");
    }

    @Test
    @DisplayName("Convoke can pay the entire cost without mana")
    void convokePaysEntireCost() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent whiteConvoker = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent firstGenericConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondGenericConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProtectiveResponse()));

        harness.castInstantWithConvoke(player1, 0, List.of(attacker.getId()),
                List.of(firstGenericConvoker.getId(), secondGenericConvoker.getId(), whiteConvoker.getId()));
        harness.passBothPriorities();

        assertThat(whiteConvoker.isTapped()).isTrue();
        assertThat(firstGenericConvoker.isTapped()).isTrue();
        assertThat(secondGenericConvoker.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Protective Response");
    }

    @Test
    @DisplayName("Does not destroy a target that stops attacking before resolution")
    void targetStopsAttackingBeforeResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Protective Response");
    }

    @Test
    @DisplayName("Can destroy the caster's own attacking creature")
    void destroysOwnAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped creatures cannot pay for convoke")
    void rejectsTappedConvoker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        convoker.tap();
        harness.setHand(player1, List.of(new ProtectiveResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(attacker.getId()), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
