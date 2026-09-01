package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.d.DiabolicMachine;
import com.github.laxika.magicalvibes.cards.n.NiallSilvain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Runesword.class, BogRats.class, DiabolicMachine.class, NiallSilvain.class})
class RuneswordTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the targeted attacking creature")
    void boostsTargetAttackingCreature() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();
        int basePower = gqs.getEffectivePower(gd, attacker);

        activate(sword, attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 2);
        assertThat(sword.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by an opponent")
    void targetsOpponentsAttackingCreature() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker(player2);
        int basePower = gqs.getEffectivePower(gd, attacker);

        activate(sword, attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();
        int basePower = gqs.getEffectivePower(gd, attacker);

        activate(sword, attacker);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Sacrifices itself when the targeted creature leaves")
    void sacrificesWhenTargetLeaves() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();

        activate(sword, attacker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, attacker));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runesword");
    }

    @Test
    @DisplayName("Exiles a creature damaged by the targeted creature instead of letting it regenerate")
    void exilesCreatureDamagedByTarget() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();
        Permanent creature = addCreatureReady(player2, new NiallSilvain());
        creature.setRegenerationShield(1);

        activate(sword, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Niall Silvain");
        harness.assertNotInGraveyard(player2, "Niall Silvain");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Niall Silvain"))).isTrue();
    }

    @Test
    @DisplayName("Exiles a creature that dies at the same time as the targeted creature")
    void exilesCreatureDyingAtSameTimeAsTarget() {
        Permanent sword = addSwordReady();
        Permanent attacker = addBogRatsAttacker();
        addCreatureReady(player2, new NiallSilvain());

        activate(sword, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bog Rats");
        harness.assertNotInGraveyard(player2, "Niall Silvain");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Niall Silvain"))).isTrue();
    }

    @Test
    @DisplayName("Requires an attacking creature as its target")
    void requiresAttackingCreature() {
        Permanent sword = addSwordReady();
        Permanent creature = addCreatureReady(player1, new NiallSilvain());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sword), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwordReady() {
        return harness.addToBattlefieldAndReturn(player1, new Runesword());
    }

    private Permanent addAttacker() {
        return addAttacker(player1);
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new DiabolicMachine());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBogRatsAttacker() {
        Permanent attacker = addCreatureReady(player1, new BogRats());
        attacker.setAttacking(true);
        return attacker;
    }

    private void activate(Permanent sword, Permanent attacker) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(sword), 0, null, attacker.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
