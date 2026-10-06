package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.IzzetCharm;
import com.github.laxika.magicalvibes.cards.m.MazeGlider;
import com.github.laxika.magicalvibes.cards.r.RunnersBane;
import com.github.laxika.magicalvibes.cards.t.ThrashingMossdog;
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

@CardUsed({Skylasher.class, Cancel.class, GiantGrowth.class, IzzetCharm.class,
        MazeGlider.class, ThrashingMossdog.class, RunnersBane.class})
class SkylasherTest extends BaseCardTest {

    @Test
    @DisplayName("Skylasher cannot be countered by Cancel")
    void cannotBeCountered() {
        Skylasher skylasher = new Skylasher();
        harness.setHand(player1, List.of(skylasher));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, skylasher.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skylasher");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Blue spell cannot target Skylasher")
    void blueSpellCannotTarget() {
        Permanent skylasher = addCreatureReady(player2, new Skylasher());

        harness.setHand(player1, List.of(new IzzetCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(skylasher.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Green spell can target Skylasher")
    void greenSpellCanTarget() {
        Permanent skylasher = addCreatureReady(player1, new Skylasher());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, skylasher.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Blue creature cannot block Skylasher")
    void blueCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new Skylasher());
        attacker.setAttacking(true);

        addCreatureReady(player2, new MazeGlider());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Skylasher takes no combat damage from a blue creature")
    void takesNoDamageFromBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new MazeGlider());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Skylasher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Skylasher");
    }

    @Test
    @DisplayName("Skylasher dies to combat damage from a non-blue creature")
    void diesToNonBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new ThrashingMossdog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Skylasher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertInGraveyard(player2, "Skylasher");
    }

    @Test
    @DisplayName("Skylasher can be cast during the opponent's upkeep")
    void flashAllowsCastingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Skylasher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skylasher");
    }

    @Test
    @DisplayName("Reach allows Skylasher to block a flying blue creature")
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new MazeGlider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Skylasher());
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Skylasher");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blue Aura cannot target Skylasher")
    void blueAuraCannotEnchantSkylasher() {
        Permanent skylasher = addCreatureReady(player2, new Skylasher());
        harness.setHand(player1, List.of(new RunnersBane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, skylasher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("A blue Aura attached without targeting falls off Skylasher")
    void attachedBlueAuraIsPutIntoGraveyard() {
        Permanent skylasher = addCreatureReady(player2, new Skylasher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RunnersBane());
        aura.setAttachedTo(skylasher.getId());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Runner's Bane");
        harness.assertInGraveyard(player1, "Runner's Bane");
        harness.assertOnBattlefield(player2, "Skylasher");
    }
}
