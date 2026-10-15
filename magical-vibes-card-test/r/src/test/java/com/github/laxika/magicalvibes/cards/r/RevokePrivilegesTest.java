package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DroverGrizzly;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevokePrivileges.class, DukharaPeafowl.class, RenegadeFreighter.class, DroverGrizzly.class})
class RevokePrivilegesTest extends BaseCardTest {

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from attacking")
    void preventsAttacking() {
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        attachTo(creature, player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from blocking")
    void preventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new DukharaPeafowl());
        attachTo(blocker, player1);
        Permanent attacker = addCreatureReady(player1, new DukharaPeafowl());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from crewing Vehicles")
    void preventsCrewing() {
        harness.addToBattlefield(player1, new RenegadeFreighter());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        attachTo(creature, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another creature can crew while the enchanted creature is restricted")
    void anotherCreatureCanCrew() {
        harness.addToBattlefield(player1, new RenegadeFreighter());
        Permanent restricted = addCreatureReady(player1, new DukharaPeafowl());
        attachTo(restricted, player2);
        Permanent unrestricted = addCreatureReady(player1, new DukharaPeafowl());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(unrestricted.isTapped()).isTrue();
        assertThat(restricted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revoke Privileges can target only a creature")
    void targetsOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RenegadeFreighter());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A resolved Revoke Privileges attaches to an opposing creature and prevents crewing")
    void resolvesOntoOpposingCreature() {
        harness.addToBattlefield(player2, new RenegadeFreighter());
        Permanent creature = addCreatureReady(player2, new DukharaPeafowl());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Revoke Privileges");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Revoke Privileges allows the creature to crew again")
    void removingAuraRestoresCrewing() {
        harness.addToBattlefield(player1, new RenegadeFreighter());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        Permanent aura = attachTo(creature, player2);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature enchanted with Revoke Privileges can still saddle a Mount")
    void enchantedCreatureCanSaddle() {
        Permanent mount = addCreatureReady(player1, new DroverGrizzly());
        Permanent creature = addCreatureReady(player1, new DukharaPeafowl());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(mount.isSaddled()).isTrue();
    }

    private Permanent attachTo(Permanent creature, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new RevokePrivileges());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
