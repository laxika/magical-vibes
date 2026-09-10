package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.v.ValorMadeReal;
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

@CardUsed({SustainerOfTheRealm.class, GiantCockroach.class})
class SustainerOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent sustainer = addCreatureReady(player2, new SustainerOfTheRealm());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(sustainer.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +0/+2 until end of turn")
    void blockTriggerGivesPlusZeroPlusTwo() {
        Permanent sustainer = addCreatureReady(player2, new SustainerOfTheRealm());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sustainer.getPowerModifier()).isEqualTo(0);
        assertThat(sustainer.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("+0/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent sustainer = addCreatureReady(player2, new SustainerOfTheRealm());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sustainer.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sustainer.getPowerModifier()).isEqualTo(0);
        assertThat(sustainer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No trigger fires when the creature is not blocking")
    void noTriggerWhenNotBlocking() {
        addCreatureReady(player2, new SustainerOfTheRealm());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(ValorMadeReal.class)
    @DisplayName("Blocking multiple creatures creates only one block trigger")
    void blockTriggerFiresOnlyOnceWhenBlockingMultipleCreatures() {
        Permanent sustainer = addCreatureReady(player2, new SustainerOfTheRealm());
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sustainer);
        addAttacker();
        addAttacker();

        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, sustainer.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ));

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
