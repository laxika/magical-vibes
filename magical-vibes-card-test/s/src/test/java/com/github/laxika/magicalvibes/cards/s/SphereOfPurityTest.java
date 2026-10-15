package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfPurity.class, GraniteShard.class, ShrapnelBlast.class, MyrEnforcer.class})
class SphereOfPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from an artifact source")
    void preventsDamageFromArtifactSource() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.addToBattlefield(player2, new GraniteShard());
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a non-artifact source")
    void doesNotPreventNonArtifactDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Prevents 1 of combat damage from an artifact creature")
    void preventsArtifactCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Prevents 1 damage from each artifact source in combat")
    void preventsDamageFromEachArtifactSource() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new MyrEnforcer());
        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Multiple copies each prevent one damage from an artifact")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents each separate damage event from the same artifact")
    void preventsRepeatedDamageFromSameArtifact() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        Permanent shard = harness.addToBattlefieldAndReturn(player2, new GraniteShard());
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        shard.untap();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents damage from your own artifact but does not protect your opponent")
    void onlyProtectsControllerRegardlessOfSourceController() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        shard.untap();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent artifact combat damage to your creatures")
    void doesNotProtectCreatures() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        addCreatureReady(player1, new MyrEnforcer());
        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Myr Enforcer");
        harness.assertInGraveyard(player2, "Myr Enforcer");
    }
}
