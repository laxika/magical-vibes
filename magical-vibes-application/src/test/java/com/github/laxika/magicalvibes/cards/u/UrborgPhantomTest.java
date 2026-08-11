package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrborgPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Urborg Phantom can't block")
    void cannotBlock() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());

        assertThat(bls.canBlock(gd, phantom)).isFalse();
    }

    @Test
    @DisplayName("Activating Urborg Phantom prevents combat damage dealt to and by it")
    void abilityPreventsCombatDamageBothWays() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(phantom.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Urborg Phantom");
    }

    @Test
    @DisplayName("Without activating the ability Urborg Phantom deals and takes combat damage normally")
    void combatDamageIsNotPreventedByDefault() {
        Permanent phantom = addCreatureReady(player2, new UrborgPhantom());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(phantom.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Urborg Phantom");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
