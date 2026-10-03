package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianPixies.class, GrizzlyBears.class, Ornithopter.class, Triskelion.class,
        ProdigalSorcerer.class, RodOfRuin.class})
class ArgothianPixiesTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent pixies = addCreatureReady(player1, new ArgothianPixies());
        pixies.setAttacking(true);

        addCreatureReady(player2, new Ornithopter());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a non-artifact creature")
    void canBeBlockedByNonArtifactCreature() {
        Permanent pixies = addCreatureReady(player1, new ArgothianPixies());
        pixies.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage from artifact creatures")
    void preventsCombatDamageFromArtifactCreatures() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        pixies.setBlocking(true);
        pixies.addBlockingTarget(0);

        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new Triskelion());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Argothian Pixies");
        assertThat(pixies.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from non-artifact creatures")
    void doesNotPreventCombatDamageFromNonArtifactCreatures() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        pixies.setBlocking(true);
        pixies.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Argothian Pixies");
        harness.assertInGraveyard(player2, "Argothian Pixies");
    }

    @Test
    @DisplayName("Prevents noncombat damage from an artifact creature")
    void preventsNoncombatDamageFromArtifactCreature() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        Permanent triskelion = addCreatureReady(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, pixies.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Argothian Pixies");
        assertThat(pixies.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from a non-artifact creature")
    void doesNotPreventNoncombatDamageFromNonArtifactCreature() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, 0, null, pixies.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Argothian Pixies");
        harness.assertInGraveyard(player2, "Argothian Pixies");
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature artifact")
    void doesNotPreventDamageFromNoncreatureArtifact() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, pixies.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Argothian Pixies");
        harness.assertInGraveyard(player2, "Argothian Pixies");
    }

    @Test
    @DisplayName("Prevents damage when the artifact creature source dies before resolution")
    void preventsDamageFromArtifactCreatureThatLeftBattlefield() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        Permanent triskelion = addCreatureReady(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        triskelion.setMarkedDamage(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, pixies.getId());

        harness.assertNotOnBattlefield(player1, "Triskelion");
        harness.assertInGraveyard(player1, "Triskelion");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Argothian Pixies");
        assertThat(pixies.getMarkedDamage()).isZero();
    }
}
