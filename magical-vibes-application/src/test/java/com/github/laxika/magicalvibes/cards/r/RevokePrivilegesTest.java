package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RevokePrivilegesTest extends BaseCardTest {

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from attacking")
    void preventsAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from blocking")
    void preventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachTo(blocker, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Revoke Privileges prevents the enchanted creature from crewing Vehicles")
    void preventsCrewing() {
        addDreadnoughtReady();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another creature can crew while the enchanted creature is restricted")
    void anotherCreatureCanCrew() {
        addDreadnoughtReady();
        Permanent restricted = addCreatureReady(player1, new GrizzlyBears());
        attachTo(restricted, player2);
        Permanent unrestricted = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(unrestricted.isTapped()).isTrue();
        assertThat(restricted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revoke Privileges can target only a creature")
    void targetsOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachTo(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new RevokePrivileges());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private Permanent addDreadnoughtReady() {
        Permanent dreadnought = new Permanent(new DuskLegionDreadnought());
        dreadnought.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dreadnought);
        return dreadnought;
    }
}
