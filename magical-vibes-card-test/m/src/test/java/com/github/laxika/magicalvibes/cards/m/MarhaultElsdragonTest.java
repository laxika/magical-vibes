package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
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

@CardUsed({MarhaultElsdragon.class, KoboldsOfKherKeep.class, Boomerang.class})
class MarhaultElsdragonTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Marhault Elsdragon gets no rampage bonus")
    void oneBlockerGivesNoBonus() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isZero();
        assertThat(marhault.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Marhault Elsdragon gets +1/+1 until end of turn")
    void twoBlockersGivePlusOne() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        assertThat(marhault.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Marhault Elsdragon gets +2/+2 until end of turn")
    void threeBlockersGivePlusTwo() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(2);
        assertThat(marhault.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Marhault Elsdragon's rampage bonus expires at end of turn")
    void rampageBonusExpiresAtEndOfTurn() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        assertThat(marhault.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isZero();
        assertThat(marhault.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rampage counts the blockers remaining when its trigger resolves")
    void blockerRemovedBeforeResolutionReducesBonus() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        Permanent blocker = addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(marhault.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        assertThat(marhault.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing a blocker after rampage resolves does not change its bonus")
    void blockerRemovedAfterResolutionDoesNotReduceBonus() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());
        Permanent blocker = addCreatureReady(player2, new KoboldsOfKherKeep());
        addCreatureReady(player2, new KoboldsOfKherKeep());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();
        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        assertThat(marhault.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("If unblocked Marhault Elsdragon gets no rampage bonus")
    void unblockedGivesNoBonus() {
        Permanent marhault = addCreatureReady(player1, new MarhaultElsdragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(marhault.getPowerModifier()).isZero();
        assertThat(marhault.getToughnessModifier()).isZero();
    }

}
