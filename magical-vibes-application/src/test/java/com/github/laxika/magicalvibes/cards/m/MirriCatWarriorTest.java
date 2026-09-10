package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirriCatWarrior.class, Forest.class, GrizzlyBears.class})
class MirriCatWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mirri, Cat Warrior puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MirriCatWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(MirriCatWarrior.class);
    }

    @Test
    @DisplayName("Forestwalk: Mirri cannot be blocked if defending player controls a Forest")
    void forestwalkCannotBeBlockedWhenDefenderHasForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent mirri = addCreatureReady(player1, new MirriCatWarrior());
        mirri.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mirri);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk: Mirri can be blocked if defending player controls no Forest")
    void forestwalkAllowsBlockingWhenDefenderHasNoForest() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent mirri = addCreatureReady(player1, new MirriCatWarrior());
        mirri.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Mirri does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent mirri = addCreatureReady(player1, new MirriCatWarrior());

        declareAttackers(List.of(0));

        assertThat(mirri.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First strike: Mirri kills 2/2 blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent mirri = addCreatureReady(player1, new MirriCatWarrior());
        mirri.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(mirri.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
