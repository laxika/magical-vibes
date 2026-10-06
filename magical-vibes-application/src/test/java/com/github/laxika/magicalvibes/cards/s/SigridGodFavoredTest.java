package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigridGodFavored.class, GrizzlyBears.class, Unsummon.class, MaskedVandal.class, DepartTheRealm.class})
class SigridGodFavoredTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an attacking creature until Sigrid leaves")
    void exilesAttackingCreatureUntilSigridLeaves() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        castSigrid(List.of(attacker.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Sigrid, God-Favored"));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can exile a blocking creature")
    void exilesBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);

        castSigrid(List.of(blocker.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can choose no target")
    void canChooseNoTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSigrid(List.of());

        harness.assertOnBattlefield(player1, "Sigrid, God-Favored");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a creature that is not attacking or blocking")
    void rejectsNoncombatCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigridGodFavored()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("God creatures with changeling cannot block Sigrid")
    void godCreatureCannotBlockSigrid() {
        Permanent sigrid = addCreatureReady(player1, new SigridGodFavored());
        sigrid.setAttacking(true);
        addCreatureReady(player2, new MaskedVandal());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Sigrid prevents combat damage from a God creature she blocks")
    void preventsCombatDamageFromGodCreature() {
        Permanent attacker = addCreatureReady(player1, new MaskedVandal());
        attacker.setAttacking(true);
        Permanent sigrid = addCreatureReady(player2, new SigridGodFavored());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Sigrid, God-Favored");
        assertThat(sigrid.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sigrid leaving before her trigger resolves prevents the exile")
    void sourceLeavingBeforeResolutionDoesNotExile() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MaskedVandal());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new SigridGodFavored()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Sigrid, God-Favored"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Sigrid, God-Favored");
        harness.assertOnBattlefield(player2, "Masked Vandal");
    }

    @Test
    @DisplayName("A creature must still be attacking or blocking when the trigger resolves")
    void targetMustStillBeInCombatOnResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MaskedVandal());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new SigridGodFavored()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();
        attacker.setAttacking(false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Masked Vandal");
    }

    private void castSigrid(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SigridGodFavored()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }
}
