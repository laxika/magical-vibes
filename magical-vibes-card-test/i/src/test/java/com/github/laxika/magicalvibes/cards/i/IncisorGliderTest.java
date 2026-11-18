package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncisorGlider.class, EliteVanguard.class, GrizzlyBears.class})
class IncisorGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Incisor Glider boosts creatures you control when an opponent has three poison counters")
    void attackBoostsOwnCreaturesWhenOpponentHasThreePoisonCounters() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());

        Permanent otherCreature = addCreatureReady(player1, new EliteVanguard());

        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, glider)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, glider)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Incisor Glider does not boost creatures when no opponent has three poison counters")
    void attackDoesNotBoostWithoutThreePoisonCounters() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());

        Permanent otherCreature = addCreatureReady(player1, new EliteVanguard());

        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, glider)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, glider)).isEqualTo(0);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCreature)).isEqualTo(0);
    }

    @Test
    @DisplayName("The corrupted condition is checked again when the attack ability resolves")
    void losingCorruptedBeforeResolutionPreventsBoost() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, glider)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, glider)).isZero();
    }

    @Test
    @DisplayName("Reaching three poison counters after attack declaration does not create a trigger")
    void becomingCorruptedAfterAttackingDoesNotTrigger() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, glider)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, glider)).isZero();
    }

    @Test
    @DisplayName("The controller's poison counters do not satisfy corrupted")
    void ownPoisonCountersDoNotEnableBoost() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, glider)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, glider)).isZero();
    }

    @Test
    @DisplayName("The ability boosts only creatures controlled at resolution and survives its source leaving")
    void recipientsAreDeterminedAtResolution() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IncisorGlider());
        gd.playerPoisonCounters.put(player2.getId(), 4);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(glider);
        gd.playerGraveyards.get(player1.getId()).add(glider.getCard());
        Permanent earlyCreature = harness.addToBattlefieldAndReturn(player1, new IncisorGlider());
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new IncisorGlider());

        assertThat(PermanentGrants.powerBoostOf(gd, earlyCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, earlyCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, lateCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, lateCreature)).isZero();
    }

    @Test
    @DisplayName("Two attacking Gliders stack their boosts and the boosts expire after the turn")
    void multipleAttackTriggersStackUntilEndOfTurn() {
        Permanent first = addCreatureReady(player1, new IncisorGlider());
        Permanent second = addCreatureReady(player1, new IncisorGlider());
        gd.playerPoisonCounters.put(player2.getId(), 5);

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
    }
}
