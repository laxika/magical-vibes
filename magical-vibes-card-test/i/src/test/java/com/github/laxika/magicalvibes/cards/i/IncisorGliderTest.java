package com.github.laxika.magicalvibes.cards.i;

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

        assertThat(glider.getPowerModifier()).isEqualTo(1);
        assertThat(glider.getToughnessModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(1);
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

        assertThat(glider.getPowerModifier()).isEqualTo(0);
        assertThat(glider.getToughnessModifier()).isEqualTo(0);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(0);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(0);
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

        assertThat(glider.getPowerModifier()).isZero();
        assertThat(glider.getToughnessModifier()).isZero();
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

        assertThat(glider.getPowerModifier()).isZero();
        assertThat(glider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The controller's poison counters do not satisfy corrupted")
    void ownPoisonCountersDoNotEnableBoost() {
        Permanent glider = addCreatureReady(player1, new IncisorGlider());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(glider.getPowerModifier()).isZero();
        assertThat(glider.getToughnessModifier()).isZero();
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

        assertThat(earlyCreature.getPowerModifier()).isEqualTo(1);
        assertThat(earlyCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
        assertThat(lateCreature.getPowerModifier()).isZero();
        assertThat(lateCreature.getToughnessModifier()).isZero();
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

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }
}
