package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({VenomousBreath.class, BalduvianBears.class, Forest.class})
class VenomousBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature blocking the target is destroyed at end of combat, not on resolution")
    void destroysAllBlockersAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        castVenomousBreath(player1, attacker);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature the target blocks is destroyed too")
    void destroysTheAttackerTheTargetBlocks() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castVenomousBreath(player2, blocker);
        advanceThroughEndOfCombat();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Nothing is destroyed when the target was never in a block this turn")
    void unblockedTargetDestroysNothing() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        castVenomousBreath(player1, attacker);
        advanceThroughEndOfCombat();

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("A blocker declared after resolution is not destroyed")
    void doesNotDestroyBlockerDeclaredAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castVenomousBreath(player1, attacker);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("A land can't be targeted")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VenomousBreath()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castVenomousBreath(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new VenomousBreath()));
        harness.addMana(caster, ManaColor.GREEN, 4);
        harness.castInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }

    private void advanceThroughEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
