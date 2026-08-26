package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BanebladeScoundrel.class, BaneclawMarauder.class, GrizzlyBears.class})
class BanebladeScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("When it becomes blocked, each blocker gets -1/-1")
    void weakensEachBlocker() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        scoundrel.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms into Baneclaw Marauder when no spells were cast last turn")
    void transformsToBack() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        advanceToUpkeepAndResolve(player1, false);

        assertThat(scoundrel.isTransformed()).isTrue();
        assertThat(scoundrel.getCard()).isInstanceOf(BaneclawMarauder.class);
    }

    @Test
    @DisplayName("Baneclaw Marauder transforms back when a player cast two spells last turn")
    void transformsBack() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        transformToBack(scoundrel);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepAndResolve(player2, true);

        assertThat(scoundrel.isTransformed()).isFalse();
        assertThat(scoundrel.getCard()).isInstanceOf(BanebladeScoundrel.class);
    }

    @Test
    @DisplayName("Baneclaw Marauder makes the controller of a dying blocker lose 1 life")
    void blockerDeathCausesLifeLoss() {
        Permanent marauder = addBackFace(player1);
        marauder.setAttacking(true);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addBackFace(Player player) {
        Permanent marauder = addCreatureReady(player, new BanebladeScoundrel());
        transformToBack(marauder);
        return marauder;
    }

    private void transformToBack(Permanent permanent) {
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1, false);
        assertThat(permanent.isTransformed()).isTrue();
    }

    private void advanceToUpkeepAndResolve(Player activePlayer, boolean twoSpellsLastTurn) {
        if (!twoSpellsLastTurn) {
            gd.spellsCastLastTurn.clear();
        }
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
